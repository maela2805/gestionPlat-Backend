package GestionPlat.example.demo.modules.sale.service.impl;

import GestionPlat.example.demo.modules.boutique.model.Boutique;
import GestionPlat.example.demo.modules.boutique.model.BoutiqueWholesalePrice;
import GestionPlat.example.demo.modules.boutique.repository.BoutiqueRepository;
import GestionPlat.example.demo.modules.boutique.repository.BoutiqueWholesalePriceRepository;
import GestionPlat.example.demo.modules.sale.dto.CreateStoreSaleItemRequest;
import GestionPlat.example.demo.modules.sale.dto.CreateStoreSaleRequest;
import GestionPlat.example.demo.modules.sale.dto.StoreSaleDTO;
import GestionPlat.example.demo.modules.sale.dto.StoreSaleItemDTO;
import GestionPlat.example.demo.modules.sale.model.StoreSale;
import GestionPlat.example.demo.modules.sale.model.StoreSaleItem;
import GestionPlat.example.demo.modules.sale.model.StoreSaleStatus;
import GestionPlat.example.demo.modules.sale.repository.StoreSaleRepository;
import GestionPlat.example.demo.modules.sale.service.StoreSaleService;
import GestionPlat.example.demo.modules.stock.model.Product;
import GestionPlat.example.demo.modules.stock.model.StockMovement;
import GestionPlat.example.demo.modules.stock.repository.ProductRepository;
import GestionPlat.example.demo.modules.stock.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreSaleServiceImpl implements StoreSaleService {

    private final StoreSaleRepository storeSaleRepository;
    private final BoutiqueRepository boutiqueRepository;
    private final BoutiqueWholesalePriceRepository wholesalePriceRepository;
    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;

    @Override
    @Transactional(readOnly = true)
    public List<StoreSaleDTO> getAllStoreSales() {
        return storeSaleRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public StoreSaleDTO getStoreSaleById(Long id) {
        StoreSale sale = storeSaleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vente introuvable avec l'ID : " + id));
        return mapToDTO(sale);
    }

    @Override
    @Transactional
    public StoreSaleDTO createStoreSale(CreateStoreSaleRequest request, String userEmail) {
        Boutique boutique = boutiqueRepository.findById(request.getBoutiqueId())
                .orElseThrow(() -> new RuntimeException("Boutique introuvable avec l'ID : " + request.getBoutiqueId()));

        String reference = request.getReference();
        if (reference == null || reference.isBlank()) {
            reference = "VTE-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        }

        if (storeSaleRepository.existsByReference(reference)) {
            throw new RuntimeException("Une vente existe déjà avec la référence : " + reference);
        }

        StoreSale sale = StoreSale.builder()
                .reference(reference)
                .boutique(boutique)
                .saleDate(LocalDateTime.now())
                .status(StoreSaleStatus.BROUILLON)
                .totalAmount(BigDecimal.ZERO)
                .userEmail(userEmail != null ? userEmail : "SYSTEM")
                .note(request.getNote())
                .items(new ArrayList<>())
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CreateStoreSaleItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new RuntimeException("Produit introuvable avec l'ID : " + itemReq.getProductId()));

            BigDecimal wholesalePrice = itemReq.getWholesalePrice();
            if (wholesalePrice == null) {
                Optional<BoutiqueWholesalePrice> priceOpt = wholesalePriceRepository
                        .findByBoutiqueIdAndProductId(boutique.getId(), product.getId());
                wholesalePrice = priceOpt.isPresent() ? priceOpt.get().getWholesalePrice() : product.getSellPrice();
            }

            BigDecimal lineTotal = wholesalePrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));

            StoreSaleItem item = StoreSaleItem.builder()
                    .storeSale(sale)
                    .product(product)
                    .wholesalePrice(wholesalePrice)
                    .quantity(itemReq.getQuantity())
                    .totalPrice(lineTotal)
                    .build();

            sale.getItems().add(item);
            totalAmount = totalAmount.add(lineTotal);
        }

        sale.setTotalAmount(totalAmount);
        StoreSale saved = storeSaleRepository.save(sale);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public StoreSaleDTO validateStoreSale(Long id, String userEmail) {
        StoreSale sale = storeSaleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vente introuvable avec l'ID : " + id));

        if (sale.getStatus() == StoreSaleStatus.VALIDEE) {
            throw new RuntimeException("Cette vente à la boutique est déjà validée.");
        }
        if (sale.getStatus() == StoreSaleStatus.ANNULEE) {
            throw new RuntimeException("Impossible de valider une vente annulée.");
        }

        // Check stock availability
        for (StoreSaleItem item : sale.getItems()) {
            Product product = item.getProduct();
            int currentStock = product.getStock() != null ? product.getStock() : 0;
            if (currentStock < item.getQuantity()) {
                throw new RuntimeException("Stock central insuffisant pour le produit '" + product.getName() +
                        "' (Stock disponible : " + currentStock + ", Quantité demandée : " + item.getQuantity() + ")");
            }
        }

        // Deduct stock & create stock movements
        for (StoreSaleItem item : sale.getItems()) {
            Product product = item.getProduct();
            int currentStock = product.getStock() != null ? product.getStock() : 0;
            product.setStock(currentStock - item.getQuantity());
            productRepository.save(product);

            StockMovement movement = StockMovement.builder()
                    .product(product)
                    .quantity(item.getQuantity())
                    .type(StockMovement.MovementType.SORTIE)
                    .reason(StockMovement.MovementReason.VENTE)
                    .userEmail(userEmail != null ? userEmail : sale.getUserEmail())
                    .note("Vente / Expédition à la Boutique " + sale.getBoutique().getName() + " (Réf: " + sale.getReference() + ")")
                    .build();

            stockMovementRepository.save(movement);
        }

        sale.setStatus(StoreSaleStatus.VALIDEE);
        return mapToDTO(storeSaleRepository.save(sale));
    }

    @Override
    @Transactional
    public StoreSaleDTO cancelStoreSale(Long id) {
        StoreSale sale = storeSaleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vente introuvable avec l'ID : " + id));

        if (sale.getStatus() == StoreSaleStatus.VALIDEE) {
            throw new RuntimeException("Impossible d'annuler une vente déjà validée et expédiée.");
        }

        sale.setStatus(StoreSaleStatus.ANNULEE);
        return mapToDTO(storeSaleRepository.save(sale));
    }

    private StoreSaleDTO mapToDTO(StoreSale sale) {
        List<StoreSaleItemDTO> itemDTOs = sale.getItems().stream().map(item ->
                StoreSaleItemDTO.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productReference(item.getProduct().getReference())
                        .productName(item.getProduct().getName())
                        .wholesalePrice(item.getWholesalePrice())
                        .quantity(item.getQuantity())
                        .totalPrice(item.getTotalPrice())
                        .build()
        ).collect(Collectors.toList());

        return StoreSaleDTO.builder()
                .id(sale.getId())
                .reference(sale.getReference())
                .boutiqueId(sale.getBoutique().getId())
                .boutiqueCode(sale.getBoutique().getCode())
                .boutiqueName(sale.getBoutique().getName())
                .saleDate(sale.getSaleDate())
                .status(sale.getStatus())
                .totalAmount(sale.getTotalAmount())
                .userEmail(sale.getUserEmail())
                .note(sale.getNote())
                .items(itemDTOs)
                .createdAt(sale.getCreatedAt())
                .build();
    }
}
