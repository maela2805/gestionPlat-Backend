package GestionPlat.example.demo.modules.pos.service.impl;

import GestionPlat.example.demo.modules.boutique.model.Boutique;
import GestionPlat.example.demo.modules.boutique.model.BoutiqueWholesalePrice;
import GestionPlat.example.demo.modules.boutique.repository.BoutiqueRepository;
import GestionPlat.example.demo.modules.boutique.repository.BoutiqueWholesalePriceRepository;
import GestionPlat.example.demo.modules.caisse.model.CashMovement;
import GestionPlat.example.demo.modules.caisse.model.CashMovementType;
import GestionPlat.example.demo.modules.caisse.model.CashSession;
import GestionPlat.example.demo.modules.caisse.model.CashSessionStatus;
import GestionPlat.example.demo.modules.caisse.repository.CashMovementRepository;
import GestionPlat.example.demo.modules.caisse.repository.CashSessionRepository;
import GestionPlat.example.demo.modules.pos.dto.CreatePosSaleItemRequest;
import GestionPlat.example.demo.modules.pos.dto.CreatePosSaleRequest;
import GestionPlat.example.demo.modules.pos.dto.PosSaleDTO;
import GestionPlat.example.demo.modules.pos.dto.PosSaleItemDTO;
import GestionPlat.example.demo.modules.pos.model.PaymentMethod;
import GestionPlat.example.demo.modules.pos.model.PosSale;
import GestionPlat.example.demo.modules.pos.model.PosSaleItem;
import GestionPlat.example.demo.modules.pos.model.PosSaleStatus;
import GestionPlat.example.demo.modules.pos.repository.PosSaleRepository;
import GestionPlat.example.demo.modules.pos.service.PosSaleService;
import GestionPlat.example.demo.modules.stock.model.BoutiqueStock;
import GestionPlat.example.demo.modules.stock.model.Product;
import GestionPlat.example.demo.modules.stock.model.StockMovement;
import GestionPlat.example.demo.modules.stock.repository.BoutiqueStockRepository;
import GestionPlat.example.demo.modules.stock.repository.ProductRepository;
import GestionPlat.example.demo.modules.stock.repository.StockMovementRepository;
import GestionPlat.example.demo.modules.tiers.model.Tiers;
import GestionPlat.example.demo.modules.tiers.repository.TiersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PosSaleServiceImpl implements PosSaleService {

    private final PosSaleRepository posSaleRepository;
    private final CashSessionRepository cashSessionRepository;
    private final CashMovementRepository cashMovementRepository;
    private final BoutiqueRepository boutiqueRepository;
    private final BoutiqueStockRepository boutiqueStockRepository;
    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;
    private final TiersRepository tiersRepository;
    private final BoutiqueWholesalePriceRepository wholesalePriceRepository;

    @Override
    @Transactional
    public PosSaleDTO createPosSale(CreatePosSaleRequest request, String userEmail) {
        Boutique boutique = boutiqueRepository.findById(request.getBoutiqueId())
                .orElseThrow(() -> new RuntimeException("Boutique introuvable avec l'ID : " + request.getBoutiqueId()));

        // Resolve cash session
        CashSession session = null;
        if (request.getCashSessionId() != null) {
            session = cashSessionRepository.findById(request.getCashSessionId())
                    .orElseThrow(() -> new RuntimeException("Session de caisse introuvable avec l'ID : " + request.getCashSessionId()));
        } else {
            session = cashSessionRepository.findByBoutiqueIdAndStatus(boutique.getId(), CashSessionStatus.OPEN)
                    .orElse(null);
            if (session == null && userEmail != null) {
                session = cashSessionRepository.findByUserEmailAndStatus(userEmail, CashSessionStatus.OPEN)
                        .orElse(null);
            }
        }

        if (session == null || session.getStatus() == CashSessionStatus.CLOSED) {
            throw new RuntimeException("Aucune session de caisse OUVERTE pour cette boutique. Veuillez ouvrir la caisse avant d'effectuer une vente.");
        }

        Tiers client = null;
        if (request.getClientId() != null) {
            client = tiersRepository.findById(request.getClientId()).orElse(null);
        }

        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String receiptNumber = "TK-" + datePrefix + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        PaymentMethod paymentMethod = request.getPaymentMethod() != null ? request.getPaymentMethod() : PaymentMethod.ESPECES;

        PosSale sale = PosSale.builder()
                .receiptNumber(receiptNumber)
                .cashSession(session)
                .boutique(boutique)
                .client(client)
                .customClientName(request.getCustomClientName())
                .saleDate(LocalDateTime.now())
                .paymentMethod(paymentMethod)
                .status(PosSaleStatus.PAYEE)
                .userEmail(userEmail != null ? userEmail : "SYSTEM")
                .notes(request.getNotes())
                .items(new ArrayList<>())
                .build();

        BigDecimal subTotal = BigDecimal.ZERO;

        for (CreatePosSaleItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new RuntimeException("Produit introuvable avec l'ID : " + itemReq.getProductId()));

            // Check Stock availability (Boutique Stock or Product Central Stock)
            BoutiqueStock boutiqueStock = boutiqueStockRepository.findByBoutiqueIdAndProductId(boutique.getId(), product.getId())
                    .orElse(null);

            int bQty = (boutiqueStock != null && boutiqueStock.getQuantity() != null) ? boutiqueStock.getQuantity() : 0;
            int pQty = (product.getStock() != null) ? product.getStock() : 0;

            if (bQty >= itemReq.getQuantity() && boutiqueStock != null) {
                // Deduct from boutique stock
                boutiqueStock.setQuantity(bQty - itemReq.getQuantity());
                boutiqueStockRepository.save(boutiqueStock);
            } else if (pQty >= itemReq.getQuantity()) {
                // Deduct from central product stock if boutique stock entry not initialized yet
                product.setStock(pQty - itemReq.getQuantity());
                productRepository.save(product);
            } else {
                int availableMax = Math.max(bQty, pQty);
                throw new RuntimeException("Stock insuffisant pour '" + product.getName() + "' (Disponible : " + availableMax + ", Quantité demandée : " + itemReq.getQuantity() + ")");
            }

            // Record Stock Movement
            StockMovement movement = StockMovement.builder()
                    .product(product)
                    .boutique(boutique)
                    .quantity(itemReq.getQuantity())
                    .type(StockMovement.MovementType.SORTIE)
                    .reason(StockMovement.MovementReason.VENTE)
                    .userEmail(userEmail != null ? userEmail : sale.getUserEmail())
                    .note("Vente Comptoir / POS (Ticket: " + receiptNumber + " - Boutique: " + boutique.getName() + ")")
                    .build();
            stockMovementRepository.save(movement);

            // Determine unit price
            BigDecimal unitPrice = itemReq.getUnitPrice();
            if (unitPrice == null) {
                Optional<BoutiqueWholesalePrice> priceOpt = wholesalePriceRepository.findByBoutiqueIdAndProductId(boutique.getId(), product.getId());
                unitPrice = priceOpt.isPresent() ? priceOpt.get().getWholesalePrice() : product.getSellPrice();
            }
            if (unitPrice == null) {
                unitPrice = BigDecimal.ZERO;
            }

            BigDecimal discount = itemReq.getDiscount() != null ? itemReq.getDiscount() : BigDecimal.ZERO;
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity())).subtract(discount);

            PosSaleItem item = PosSaleItem.builder()
                    .posSale(sale)
                    .product(product)
                    .unitPrice(unitPrice)
                    .quantity(itemReq.getQuantity())
                    .discount(discount)
                    .totalPrice(lineTotal)
                    .build();

            sale.getItems().add(item);
            subTotal = subTotal.add(lineTotal);
        }

        BigDecimal discountAmount = request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal taxAmount = request.getTaxAmount() != null ? request.getTaxAmount() : BigDecimal.ZERO;
        BigDecimal netAmount = subTotal.subtract(discountAmount).add(taxAmount);

        BigDecimal amountPaid = request.getAmountPaid() != null ? request.getAmountPaid() : netAmount;
        BigDecimal changeReturned = paymentMethod == PaymentMethod.ESPECES ? amountPaid.subtract(netAmount).max(BigDecimal.ZERO) : BigDecimal.ZERO;

        sale.setSubTotal(subTotal);
        sale.setDiscountAmount(discountAmount);
        sale.setTaxAmount(taxAmount);
        sale.setNetAmount(netAmount);
        sale.setAmountPaid(amountPaid);
        sale.setChangeReturned(changeReturned);

        PosSale savedSale = posSaleRepository.save(sale);

        // Update Cash Session totals
        updateCashSessionTotals(session, paymentMethod, netAmount);

        return mapToDTO(savedSale);
    }

    private void updateCashSessionTotals(CashSession session, PaymentMethod paymentMethod, BigDecimal netAmount) {
        if (paymentMethod == PaymentMethod.ESPECES) {
            session.setTotalSalesCash(session.getTotalSalesCash().add(netAmount));
        } else if (paymentMethod == PaymentMethod.WAVE || paymentMethod == PaymentMethod.ORANGE_MONEY) {
            session.setTotalSalesMobileMoney(session.getTotalSalesMobileMoney().add(netAmount));
        } else if (paymentMethod == PaymentMethod.CARTE) {
            session.setTotalSalesCard(session.getTotalSalesCard().add(netAmount));
        } else {
            session.setTotalSalesOther(session.getTotalSalesOther().add(netAmount));
        }

        // Recalculate expected closing balance for cash
        List<CashMovement> movements = cashMovementRepository.findByCashSessionIdOrderByCreatedAtDesc(session.getId());
        BigDecimal totalCashIn = movements.stream().filter(m -> m.getType() == CashMovementType.ENTREE).map(CashMovement::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCashOut = movements.stream().filter(m -> m.getType() == CashMovementType.SORTIE).map(CashMovement::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal expectedCash = session.getOpeningBalance().add(session.getTotalSalesCash()).add(totalCashIn).subtract(totalCashOut);
        session.setClosingBalanceExpected(expectedCash);

        cashSessionRepository.save(session);
    }

    @Override
    @Transactional(readOnly = true)
    public PosSaleDTO getPosSaleById(Long id) {
        PosSale sale = posSaleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vente POS introuvable avec l'ID : " + id));
        return mapToDTO(sale);
    }

    @Override
    @Transactional(readOnly = true)
    public PosSaleDTO getPosSaleByReceiptNumber(String receiptNumber) {
        PosSale sale = posSaleRepository.findByReceiptNumber(receiptNumber)
                .orElseThrow(() -> new RuntimeException("Vente POS introuvable avec le numéro de ticket : " + receiptNumber));
        return mapToDTO(sale);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PosSaleDTO> getPosSalesBySession(Long cashSessionId) {
        return posSaleRepository.findByCashSessionIdOrderBySaleDateDesc(cashSessionId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PosSaleDTO> getPosSalesByBoutique(Long boutiqueId) {
        return posSaleRepository.findByBoutiqueIdOrderBySaleDateDesc(boutiqueId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PosSaleDTO> getAllPosSales() {
        return posSaleRepository.findAllByOrderBySaleDateDesc().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PosSaleDTO cancelPosSale(Long id, String userEmail) {
        PosSale sale = posSaleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vente POS introuvable avec l'ID : " + id));

        if (sale.getStatus() == PosSaleStatus.ANNULEE) {
            throw new RuntimeException("Cette vente est déjà annulée.");
        }

        // Restock boutique
        for (PosSaleItem item : sale.getItems()) {
            Product product = item.getProduct();
            BoutiqueStock boutiqueStock = boutiqueStockRepository.findByBoutiqueIdAndProductId(sale.getBoutique().getId(), product.getId())
                    .orElse(null);
            if (boutiqueStock != null) {
                boutiqueStock.setQuantity(boutiqueStock.getQuantity() + item.getQuantity());
                boutiqueStockRepository.save(boutiqueStock);
            }

            StockMovement movement = StockMovement.builder()
                    .product(product)
                    .boutique(sale.getBoutique())
                    .quantity(item.getQuantity())
                    .type(StockMovement.MovementType.ENTREE)
                    .reason(StockMovement.MovementReason.AJUSTEMENT)
                    .userEmail(userEmail != null ? userEmail : "SYSTEM")
                    .note("Annulation Vente Comptoir / POS (Ticket: " + sale.getReceiptNumber() + ")")
                    .build();
            stockMovementRepository.save(movement);
        }

        // Subtract from session totals if session is still open
        CashSession session = sale.getCashSession();
        if (session != null && session.getStatus() == CashSessionStatus.OPEN) {
            if (sale.getPaymentMethod() == PaymentMethod.ESPECES) {
                session.setTotalSalesCash(session.getTotalSalesCash().subtract(sale.getNetAmount()).max(BigDecimal.ZERO));
            } else if (sale.getPaymentMethod() == PaymentMethod.WAVE || sale.getPaymentMethod() == PaymentMethod.ORANGE_MONEY) {
                session.setTotalSalesMobileMoney(session.getTotalSalesMobileMoney().subtract(sale.getNetAmount()).max(BigDecimal.ZERO));
            } else if (sale.getPaymentMethod() == PaymentMethod.CARTE) {
                session.setTotalSalesCard(session.getTotalSalesCard().subtract(sale.getNetAmount()).max(BigDecimal.ZERO));
            } else {
                session.setTotalSalesOther(session.getTotalSalesOther().subtract(sale.getNetAmount()).max(BigDecimal.ZERO));
            }
            cashSessionRepository.save(session);
        }

        sale.setStatus(PosSaleStatus.ANNULEE);
        return mapToDTO(posSaleRepository.save(sale));
    }

    private PosSaleDTO mapToDTO(PosSale sale) {
        List<PosSaleItemDTO> itemDTOs = sale.getItems().stream().map(item ->
                PosSaleItemDTO.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productReference(item.getProduct().getReference())
                        .productName(item.getProduct().getName())
                        .unitPrice(item.getUnitPrice())
                        .quantity(item.getQuantity())
                        .discount(item.getDiscount())
                        .totalPrice(item.getTotalPrice())
                        .build()
        ).collect(Collectors.toList());

        String resolvedClientName = "Client Passager";
        if (sale.getClient() != null) {
            resolvedClientName = sale.getClient().getName();
        } else if (sale.getCustomClientName() != null && !sale.getCustomClientName().isBlank()) {
            resolvedClientName = sale.getCustomClientName();
        }

        return PosSaleDTO.builder()
                .id(sale.getId())
                .receiptNumber(sale.getReceiptNumber())
                .cashSessionId(sale.getCashSession().getId())
                .sessionReference(sale.getCashSession().getSessionReference())
                .boutiqueId(sale.getBoutique().getId())
                .boutiqueCode(sale.getBoutique().getCode())
                .boutiqueName(sale.getBoutique().getName())
                .clientId(sale.getClient() != null ? sale.getClient().getId() : null)
                .customClientName(sale.getCustomClientName())
                .clientName(resolvedClientName)
                .saleDate(sale.getSaleDate())
                .subTotal(sale.getSubTotal())
                .discountAmount(sale.getDiscountAmount())
                .taxAmount(sale.getTaxAmount())
                .netAmount(sale.getNetAmount())
                .amountPaid(sale.getAmountPaid())
                .changeReturned(sale.getChangeReturned())
                .paymentMethod(sale.getPaymentMethod())
                .status(sale.getStatus())
                .userEmail(sale.getUserEmail())
                .notes(sale.getNotes())
                .items(itemDTOs)
                .createdAt(sale.getCreatedAt())
                .build();
    }
}
