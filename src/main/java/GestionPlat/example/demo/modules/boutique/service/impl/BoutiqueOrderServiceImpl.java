package GestionPlat.example.demo.modules.boutique.service.impl;

import GestionPlat.example.demo.modules.billing.dto.InvoiceDTO;
import GestionPlat.example.demo.modules.billing.model.Invoice;
import GestionPlat.example.demo.modules.billing.model.InvoiceItem;
import GestionPlat.example.demo.modules.billing.model.InvoiceStatus;
import GestionPlat.example.demo.modules.billing.model.InvoiceType;
import GestionPlat.example.demo.modules.billing.repository.InvoiceRepository;
import GestionPlat.example.demo.modules.boutique.dto.*;
import GestionPlat.example.demo.modules.boutique.model.*;
import GestionPlat.example.demo.modules.boutique.repository.BoutiqueOrderRepository;
import GestionPlat.example.demo.modules.boutique.repository.BoutiqueRepository;
import GestionPlat.example.demo.modules.boutique.service.BoutiqueOrderService;
import GestionPlat.example.demo.modules.stock.dto.TransferStockRequest;
import GestionPlat.example.demo.modules.stock.model.Product;
import GestionPlat.example.demo.modules.stock.repository.ProductRepository;
import GestionPlat.example.demo.modules.stock.service.BoutiqueStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoutiqueOrderServiceImpl implements BoutiqueOrderService {

    private final BoutiqueOrderRepository boutiqueOrderRepository;
    private final BoutiqueRepository boutiqueRepository;
    private final ProductRepository productRepository;
    private final BoutiqueStockService boutiqueStockService;
    private final InvoiceRepository invoiceRepository;

    @Override
    public List<BoutiqueOrderDTO> getAllOrders() {
        return boutiqueOrderRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BoutiqueOrderDTO> getOrdersByBoutique(Long boutiqueId) {
        return boutiqueOrderRepository.findByBoutiqueIdOrderByCreatedAtDesc(boutiqueId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BoutiqueOrderDTO getOrderById(Long id) {
        BoutiqueOrder order = boutiqueOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande introuvable avec l'id : " + id));
        return mapToDTO(order);
    }

    @Override
    @Transactional
    public BoutiqueOrderDTO createOrder(CreateBoutiqueOrderRequest request, String userEmail) {
        if (request.getBoutiqueId() == null) {
            throw new RuntimeException("L'identifiant de la boutique est obligatoire.");
        }
        Boutique boutique = boutiqueRepository.findById(request.getBoutiqueId())
                .orElseThrow(() -> new RuntimeException("Boutique introuvable avec l'id : " + request.getBoutiqueId()));

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("La commande doit contenir au moins un article.");
        }

        long count = boutiqueOrderRepository.count() + 1;
        String orderNumber = String.format("CMD-BTQ-%s-%04d", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")), count);

        BoutiqueOrder order = BoutiqueOrder.builder()
                .orderNumber(orderNumber)
                .boutique(boutique)
                .orderDate(LocalDateTime.now())
                .status(BoutiqueOrderStatus.PENDING)
                .note(request.getNote())
                .createdByUser(userEmail)
                .totalAmount(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (CreateBoutiqueOrderItemRequest itemReq : request.getItems()) {
            if (itemReq.getProductId() == null || itemReq.getQuantity() == null || itemReq.getQuantity() <= 0) {
                continue;
            }
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'id : " + itemReq.getProductId()));

            BigDecimal unitPrice = product.getWholesalePrice() != null ? product.getWholesalePrice()
                    : (product.getSellPrice() != null ? product.getSellPrice() : product.getBuyPrice());

            BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            total = total.add(totalPrice);

            BoutiqueOrderItem item = BoutiqueOrderItem.builder()
                    .boutiqueOrder(order)
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(unitPrice)
                    .totalPrice(totalPrice)
                    .build();

            order.getItems().add(item);
        }

        order.setTotalAmount(total);
        BoutiqueOrder saved = boutiqueOrderRepository.save(order);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public BoutiqueOrderDTO approveOrder(Long id, ApproveBoutiqueOrderRequest request, String userEmail) {
        BoutiqueOrder order = boutiqueOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande introuvable avec l'id : " + id));

        if (order.getStatus() == BoutiqueOrderStatus.APPROVED || order.getStatus() == BoutiqueOrderStatus.DELIVERED) {
            throw new RuntimeException("Cette commande a déjà été approuvée.");
        }

        boolean isModified = false;
        if (request.getModifiedItems() != null && !request.getModifiedItems().isEmpty()) {
            isModified = true;
            order.getItems().clear();
            BigDecimal total = BigDecimal.ZERO;

            for (CreateBoutiqueOrderItemRequest modItem : request.getModifiedItems()) {
                if (modItem.getProductId() == null || modItem.getQuantity() == null || modItem.getQuantity() <= 0) {
                    continue;
                }
                Product product = productRepository.findById(modItem.getProductId())
                        .orElseThrow(() -> new RuntimeException("Produit introuvable"));

                BigDecimal unitPrice = product.getWholesalePrice() != null ? product.getWholesalePrice()
                        : (product.getSellPrice() != null ? product.getSellPrice() : product.getBuyPrice());
                BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(modItem.getQuantity()));
                total = total.add(totalPrice);

                BoutiqueOrderItem item = BoutiqueOrderItem.builder()
                        .boutiqueOrder(order)
                        .product(product)
                        .quantity(modItem.getQuantity())
                        .unitPrice(unitPrice)
                        .totalPrice(totalPrice)
                        .build();

                order.getItems().add(item);
            }
            order.setTotalAmount(total);
        }

        // Parse et mise à jour des infos de livraison
        if (request.getDeliveryDate() != null && !request.getDeliveryDate().isBlank()) {
            try {
                order.setDeliveryDate(LocalDateTime.parse(request.getDeliveryDate()));
            } catch (Exception e) {
                order.setDeliveryDate(LocalDateTime.now().plusDays(1));
            }
        } else {
            order.setDeliveryDate(LocalDateTime.now().plusDays(1));
        }

        order.setVehicleRegistration(request.getVehicleRegistration());
        order.setDriverName(request.getDriverName());
        order.setDriverPhone(request.getDriverPhone());
        if (request.getAttachmentUrl() != null) {
            order.setAttachmentUrl(request.getAttachmentUrl());
        }

        order.setStatus(isModified ? BoutiqueOrderStatus.MODIFIED_AND_APPROVED : BoutiqueOrderStatus.APPROVED);

        // Effectuer le transfert de stock réel du Dépôt Central vers la boutique destinataire
        for (BoutiqueOrderItem item : order.getItems()) {
            TransferStockRequest stockTransfer = new TransferStockRequest();
            stockTransfer.setProductId(item.getProduct().getId());
            stockTransfer.setFromBoutiqueId(null); // Dépôt Central
            stockTransfer.setToBoutiqueId(order.getBoutique().getId());
            stockTransfer.setQuantity(item.getQuantity());
            stockTransfer.setNote("Validation de la commande N° " + order.getOrderNumber());

            boutiqueStockService.transferStock(stockTransfer, userEmail);
        }

        BoutiqueOrder saved = boutiqueOrderRepository.save(order);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public BoutiqueOrderDTO rejectOrder(Long id, String reason) {
        BoutiqueOrder order = boutiqueOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande introuvable avec l'id : " + id));

        order.setStatus(BoutiqueOrderStatus.REJECTED);
        if (reason != null && !reason.isBlank()) {
            order.setNote((order.getNote() != null ? order.getNote() + " | " : "") + "Motif de refus: " + reason);
        }
        return mapToDTO(boutiqueOrderRepository.save(order));
    }

    @Override
    @Transactional
    public BoutiqueOrderDTO cancelOrder(Long id) {
        BoutiqueOrder order = boutiqueOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande introuvable avec l'id : " + id));

        if (order.getStatus() == BoutiqueOrderStatus.APPROVED || order.getStatus() == BoutiqueOrderStatus.DELIVERED) {
            throw new RuntimeException("Impossible d'annuler une commande déjà approuvée et transférée.");
        }

        order.setStatus(BoutiqueOrderStatus.CANCELLED);
        return mapToDTO(boutiqueOrderRepository.save(order));
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        BoutiqueOrder order = boutiqueOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande introuvable avec l'id : " + id));

        if (order.getStatus() == BoutiqueOrderStatus.APPROVED || order.getStatus() == BoutiqueOrderStatus.DELIVERED) {
            throw new RuntimeException("Impossible de supprimer une commande validée.");
        }

        boutiqueOrderRepository.delete(order);
    }

    @Override
    @Transactional
    public InvoiceDTO createInvoiceForOrder(Long orderId) {
        BoutiqueOrder order = boutiqueOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Commande introuvable avec l'id : " + orderId));

        if (order.getStatus() == BoutiqueOrderStatus.PENDING) {
            throw new RuntimeException("La commande doit obligatoirement être approuvée avant de pouvoir générer sa facture.");
        }

        if (Boolean.TRUE.equals(order.getInvoiceCreated())) {
            Invoice existing = invoiceRepository.findBySourceBoutiqueOrderId(orderId)
                    .orElse(null);
            if (existing != null) {
                return mapInvoiceToDTO(existing);
            }
        }

        long count = invoiceRepository.count() + 1;
        String invoiceNum = String.format("FAC-CESS-%s-%04d", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")), count);

        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNum)
                .type(InvoiceType.CESSION_BOUTIQUE)
                .status(InvoiceStatus.VALIDEE)
                .invoiceDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(30))
                .boutique(order.getBoutique())
                .sourceBoutiqueOrder(order)
                .subtotalHt(order.getTotalAmount())
                .taxRate(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .totalTtc(order.getTotalAmount())
                .paidAmount(BigDecimal.ZERO)
                .remainingAmount(order.getTotalAmount())
                .note("Facture de cession issue de la commande N° " + order.getOrderNumber())
                .items(new ArrayList<>())
                .build();

        for (BoutiqueOrderItem item : order.getItems()) {
            InvoiceItem invItem = InvoiceItem.builder()
                    .invoice(invoice)
                    .description(item.getProduct().getName())
                    .quantity(BigDecimal.valueOf(item.getQuantity()))
                    .unitPriceHt(item.getUnitPrice())
                    .taxRate(BigDecimal.ZERO)
                    .totalHt(item.getTotalPrice())
                    .totalTtc(item.getTotalPrice())
                    .build();
            invoice.getItems().add(invItem);
        }

        Invoice savedInvoice = invoiceRepository.save(invoice);
        order.setInvoiceCreated(true);
        boutiqueOrderRepository.save(order);

        return mapInvoiceToDTO(savedInvoice);
    }

    private BoutiqueOrderDTO mapToDTO(BoutiqueOrder order) {
        Invoice invoice = invoiceRepository.findBySourceBoutiqueOrderId(order.getId()).orElse(null);

        List<BoutiqueOrderItemDTO> itemDTOs = order.getItems().stream()
                .map(i -> BoutiqueOrderItemDTO.builder()
                        .id(i.getId())
                        .productId(i.getProduct().getId())
                        .productReference(i.getProduct().getReference())
                        .productName(i.getProduct().getName())
                        .productImageUrl(i.getProduct().getImageUrl())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .totalPrice(i.getTotalPrice())
                        .build())
                .collect(Collectors.toList());

        return BoutiqueOrderDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .boutiqueId(order.getBoutique() != null ? order.getBoutique().getId() : null)
                .boutiqueName(order.getBoutique() != null ? order.getBoutique().getName() : "Dépôt Central")
                .orderDate(order.getOrderDate())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .deliveryDate(order.getDeliveryDate())
                .vehicleRegistration(order.getVehicleRegistration())
                .driverName(order.getDriverName())
                .driverPhone(order.getDriverPhone())
                .attachmentUrl(order.getAttachmentUrl())
                .boutiqueSignature(order.getBoutiqueSignature())
                .depotSignature(order.getDepotSignature())
                .invoiceCreated(order.getInvoiceCreated())
                .invoiceId(invoice != null ? invoice.getId() : null)
                .invoiceNumber(invoice != null ? invoice.getInvoiceNumber() : null)
                .note(order.getNote())
                .createdByUser(order.getCreatedByUser())
                .items(itemDTOs)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private InvoiceDTO mapInvoiceToDTO(Invoice invoice) {
        return InvoiceDTO.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .type(invoice.getType())
                .status(invoice.getStatus())
                .invoiceDate(invoice.getInvoiceDate())
                .dueDate(invoice.getDueDate())
                .boutiqueId(invoice.getBoutique() != null ? invoice.getBoutique().getId() : null)
                .boutiqueName(invoice.getBoutique() != null ? invoice.getBoutique().getName() : null)
                .subtotalHt(invoice.getSubtotalHt())
                .taxRate(invoice.getTaxRate())
                .taxAmount(invoice.getTaxAmount())
                .totalTtc(invoice.getTotalTtc())
                .paidAmount(invoice.getPaidAmount())
                .remainingAmount(invoice.getRemainingAmount())
                .note(invoice.getNote())
                .build();
    }
}
