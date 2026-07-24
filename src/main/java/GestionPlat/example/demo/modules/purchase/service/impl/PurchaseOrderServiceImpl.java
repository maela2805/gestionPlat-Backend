package GestionPlat.example.demo.modules.purchase.service.impl;

import GestionPlat.example.demo.modules.purchase.dto.CreatePurchaseOrderItemRequest;
import GestionPlat.example.demo.modules.purchase.dto.CreatePurchaseOrderRequest;
import GestionPlat.example.demo.modules.purchase.dto.PurchaseOrderDTO;
import GestionPlat.example.demo.modules.purchase.dto.PurchaseOrderItemDTO;
import GestionPlat.example.demo.modules.purchase.model.PurchaseOrder;
import GestionPlat.example.demo.modules.purchase.model.PurchaseOrderItem;
import GestionPlat.example.demo.modules.purchase.model.PurchaseOrderStatus;
import GestionPlat.example.demo.modules.purchase.repository.PurchaseOrderRepository;
import GestionPlat.example.demo.modules.purchase.service.PurchaseOrderService;
import GestionPlat.example.demo.modules.stock.model.Product;
import GestionPlat.example.demo.modules.stock.model.StockMovement;
import GestionPlat.example.demo.modules.stock.repository.ProductRepository;
import GestionPlat.example.demo.modules.stock.repository.StockMovementRepository;
import GestionPlat.example.demo.modules.tiers.model.Tiers;
import GestionPlat.example.demo.modules.tiers.repository.TiersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import GestionPlat.example.demo.modules.stock.model.Category;
import GestionPlat.example.demo.modules.stock.repository.CategoryRepository;

@Service
@RequiredArgsConstructor
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final TiersRepository tiersRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StockMovementRepository stockMovementRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrderDTO> getAllPurchaseOrders() {
        return purchaseOrderRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseOrderDTO getPurchaseOrderById(Long id) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande introuvable avec l'ID : " + id));
        return mapToDTO(order);
    }

    @Override
    @Transactional
    public PurchaseOrderDTO createPurchaseOrder(CreatePurchaseOrderRequest request) {
        Tiers supplier = tiersRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new RuntimeException("Fournisseur introuvable avec l'ID : " + request.getSupplierId()));

        String reference = request.getReference();
        if (reference == null || reference.isBlank()) {
            reference = "CMD-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        }

        if (purchaseOrderRepository.existsByReference(reference)) {
            throw new RuntimeException("Une commande existe déjà avec la référence : " + reference);
        }

        PurchaseOrder order = PurchaseOrder.builder()
                .reference(reference)
                .supplier(supplier)
                .orderDate(LocalDateTime.now())
                .expectedDeliveryDate(request.getExpectedDeliveryDate())
                .status(PurchaseOrderStatus.BROUILLON)
                .totalAmount(BigDecimal.ZERO)
                .note(request.getNote())
                .items(new ArrayList<>())
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CreatePurchaseOrderItemRequest itemReq : request.getItems()) {
            Product product = null;
            String pendingName = null;
            String pendingRef = null;
            Long pendingCatId = null;

            if (itemReq.getProductId() != null) {
                product = productRepository.findById(itemReq.getProductId())
                        .orElseThrow(() -> new RuntimeException("Produit introuvable avec l'ID : " + itemReq.getProductId()));
            } else if (itemReq.getProductName() != null && !itemReq.getProductName().isBlank()) {
                pendingName = itemReq.getProductName().trim();
                pendingRef = itemReq.getProductReference() != null ? itemReq.getProductReference().trim() : null;
                pendingCatId = itemReq.getCategoryId();
            } else {
                throw new RuntimeException("Chaque ligne de commande doit soit spécifier un produit existant, soit le nom d'un nouveau produit.");
            }

            BigDecimal lineTotal = itemReq.getUnitPrice().multiply(BigDecimal.valueOf(itemReq.getQuantityOrdered()));

            PurchaseOrderItem item = PurchaseOrderItem.builder()
                    .purchaseOrder(order)
                    .product(product)
                    .pendingProductName(pendingName)
                    .pendingProductRef(pendingRef)
                    .pendingCategoryId(pendingCatId)
                    .unitPrice(itemReq.getUnitPrice())
                    .quantityOrdered(itemReq.getQuantityOrdered())
                    .quantityReceived(0)
                    .totalPrice(lineTotal)
                    .build();

            order.getItems().add(item);
            totalAmount = totalAmount.add(lineTotal);
        }

        order.setTotalAmount(totalAmount);
        PurchaseOrder saved = purchaseOrderRepository.save(order);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public PurchaseOrderDTO validatePurchaseOrder(Long id) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande introuvable avec l'ID : " + id));

        if (order.getStatus() != PurchaseOrderStatus.BROUILLON) {
            throw new RuntimeException("Seule une commande au statut BROUILLON peut être validée.");
        }

        order.setStatus(PurchaseOrderStatus.VALIDEE);
        return mapToDTO(purchaseOrderRepository.save(order));
    }

    @Override
    @Transactional
    public PurchaseOrderDTO receiveDelivery(Long id, String userEmail) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande introuvable avec l'ID : " + id));

        if (order.getStatus() == PurchaseOrderStatus.LIVREE) {
            throw new RuntimeException("Cette commande a déjà été réceptionnée et livrée.");
        }
        if (order.getStatus() == PurchaseOrderStatus.ANNULEE) {
            throw new RuntimeException("Impossible de réceptionner une commande annulée.");
        }

        order.setStatus(PurchaseOrderStatus.LIVREE);
        order.setDeliveryDate(LocalDateTime.now());

        for (PurchaseOrderItem item : order.getItems()) {
            item.setQuantityReceived(item.getQuantityOrdered());

            Product product = item.getProduct();

            // If the item is a new pending product, create it in DB NOW upon delivery!
            if (product == null && item.getPendingProductName() != null) {
                String ref = item.getPendingProductRef();
                if (ref == null || ref.isBlank()) {
                    long count = productRepository.count() + 1;
                    ref = String.format("PROD-%05d", count);
                }
                if (productRepository.existsByReference(ref)) {
                    ref = "PROD-" + System.currentTimeMillis() % 100000;
                }

                Category category = null;
                if (item.getPendingCategoryId() != null) {
                    category = categoryRepository.findById(item.getPendingCategoryId()).orElse(null);
                }

                product = Product.builder()
                        .reference(ref)
                        .name(item.getPendingProductName())
                        .buyPrice(item.getUnitPrice())
                        .sellPrice(item.getUnitPrice())
                        .stock(item.getQuantityOrdered())
                        .alertThreshold(5)
                        .category(category)
                        .build();

                product = productRepository.save(product);
                item.setProduct(product);
            } else if (product != null) {
                product.setStock(product.getStock() + item.getQuantityOrdered());
                if (item.getUnitPrice() != null && item.getUnitPrice().compareTo(BigDecimal.ZERO) > 0) {
                    product.setBuyPrice(item.getUnitPrice());
                }
                productRepository.save(product);
            }

            StockMovement movement = StockMovement.builder()
                    .product(product)
                    .quantity(item.getQuantityOrdered())
                    .type(StockMovement.MovementType.ENTREE)
                    .reason(StockMovement.MovementReason.REAPPROVISIONNEMENT)
                    .userEmail(userEmail != null ? userEmail : "SYSTEM")
                    .note("Réception Livraison Commande " + order.getReference() + " (" + order.getSupplier().getName() + ")")
                    .build();

            stockMovementRepository.save(movement);
        }

        return mapToDTO(purchaseOrderRepository.save(order));
    }

    @Override
    @Transactional
    public PurchaseOrderDTO cancelPurchaseOrder(Long id) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande introuvable avec l'ID : " + id));

        if (order.getStatus() == PurchaseOrderStatus.LIVREE) {
            throw new RuntimeException("Impossible d'annuler une commande déjà livrée.");
        }

        order.setStatus(PurchaseOrderStatus.ANNULEE);
        return mapToDTO(purchaseOrderRepository.save(order));
    }

    private PurchaseOrderDTO mapToDTO(PurchaseOrder order) {
        List<PurchaseOrderItemDTO> itemDTOs = order.getItems().stream().map(item -> {
            boolean isPending = (item.getProduct() == null);
            Long pId = !isPending ? item.getProduct().getId() : null;
            String pRef = !isPending ? item.getProduct().getReference() : item.getPendingProductRef();
            String pName = !isPending ? item.getProduct().getName() : item.getPendingProductName();

            return PurchaseOrderItemDTO.builder()
                    .id(item.getId())
                    .productId(pId)
                    .productReference(pRef)
                    .productName(pName)
                    .unitPrice(item.getUnitPrice())
                    .quantityOrdered(item.getQuantityOrdered())
                    .quantityReceived(item.getQuantityReceived())
                    .totalPrice(item.getTotalPrice())
                    .isPendingProduct(isPending)
                    .build();
        }).collect(Collectors.toList());

        return PurchaseOrderDTO.builder()
                .id(order.getId())
                .reference(order.getReference())
                .supplierId(order.getSupplier().getId())
                .supplierCode(order.getSupplier().getCode())
                .supplierName(order.getSupplier().getName())
                .orderDate(order.getOrderDate())
                .expectedDeliveryDate(order.getExpectedDeliveryDate())
                .deliveryDate(order.getDeliveryDate())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .note(order.getNote())
                .items(itemDTOs)
                .createdAt(order.getCreatedAt())
                .build();
    }
}
