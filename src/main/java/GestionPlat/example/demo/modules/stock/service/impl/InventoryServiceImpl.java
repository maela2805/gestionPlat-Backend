package GestionPlat.example.demo.modules.stock.service.impl;

import GestionPlat.example.demo.modules.boutique.model.Boutique;
import GestionPlat.example.demo.modules.boutique.repository.BoutiqueRepository;
import GestionPlat.example.demo.modules.stock.dto.CreateInventoryRequest;
import GestionPlat.example.demo.modules.stock.dto.InventoryItemRequest;
import GestionPlat.example.demo.modules.stock.model.*;
import GestionPlat.example.demo.modules.stock.model.StockMovement.MovementReason;
import GestionPlat.example.demo.modules.stock.model.StockMovement.MovementType;
import GestionPlat.example.demo.modules.stock.repository.BoutiqueStockRepository;
import GestionPlat.example.demo.modules.stock.repository.InventoryRepository;
import GestionPlat.example.demo.modules.stock.repository.ProductRepository;
import GestionPlat.example.demo.modules.stock.repository.StockMovementRepository;
import GestionPlat.example.demo.modules.stock.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final BoutiqueRepository boutiqueRepository;
    private final ProductRepository productRepository;
    private final BoutiqueStockRepository boutiqueStockRepository;
    private final StockMovementRepository stockMovementRepository;

    @Override
    @Transactional
    public Inventory createInventory(CreateInventoryRequest request, String userEmail) {
        Boutique boutique = null;
        if (request.getBoutiqueId() != null) {
            boutique = boutiqueRepository.findById(request.getBoutiqueId())
                    .orElseThrow(() -> new RuntimeException("Boutique non trouvée avec l'id: " + request.getBoutiqueId()));
        }

        String boutiqueCode = boutique != null ? boutique.getCode() : "CENTRAL";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        String ref = "INV-" + boutiqueCode + "-" + timestamp;

        Inventory inventory = Inventory.builder()
                .reference(ref)
                .boutique(boutique)
                .status(InventoryStatus.BROUILLON)
                .note(request.getNote())
                .userEmail(userEmail)
                .items(new ArrayList<>())
                .build();

        if (request.getItems() != null) {
            for (InventoryItemRequest itemReq : request.getItems()) {
                Product product = productRepository.findById(itemReq.getProductId())
                        .orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'id: " + itemReq.getProductId()));

                int theoretical;
                if (boutique != null) {
                    theoretical = boutiqueStockRepository.findByBoutiqueIdAndProductId(boutique.getId(), product.getId())
                            .map(bs -> bs.getQuantity() != null ? bs.getQuantity() : 0)
                            .orElse(0);
                } else {
                    theoretical = product.getStock() != null ? product.getStock() : 0;
                }

                int counted = itemReq.getCountedQuantity() != null ? itemReq.getCountedQuantity() : 0;
                int gap = counted - theoretical;

                InventoryItem item = InventoryItem.builder()
                        .inventory(inventory)
                        .product(product)
                        .theoreticalQuantity(theoretical)
                        .countedQuantity(counted)
                        .gap(gap)
                        .build();

                inventory.getItems().add(item);
            }
        }

        return inventoryRepository.save(inventory);
    }

    @Override
    @Transactional
    public Inventory validateInventory(Long inventoryId, String userEmail) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException("Inventaire non trouvé avec l'id: " + inventoryId));

        if (inventory.getStatus() == InventoryStatus.VALIDE) {
            throw new RuntimeException("Cet inventaire est déjà validé.");
        }

        Boutique boutique = inventory.getBoutique();

        for (InventoryItem item : inventory.getItems()) {
            Product product = item.getProduct();
            int gap = item.getGap();

            if (gap != 0) {
                MovementType type = gap > 0 ? MovementType.ENTREE : MovementType.SORTIE;
                int qty = Math.abs(gap);

                if (boutique != null) {
                    // Update Boutique stock
                    BoutiqueStock bStock = boutiqueStockRepository.findByBoutiqueIdAndProductId(boutique.getId(), product.getId())
                            .orElseGet(() -> BoutiqueStock.builder()
                                    .boutique(boutique)
                                    .product(product)
                                    .quantity(0)
                                    .build());
                    bStock.setQuantity(item.getCountedQuantity());
                    boutiqueStockRepository.save(bStock);
                } else {
                    // Update Central product stock
                    product.setStock(item.getCountedQuantity());
                    productRepository.save(product);
                }

                // Record stock movement for audit
                StockMovement movement = StockMovement.builder()
                        .product(product)
                        .boutique(boutique)
                        .quantity(qty)
                        .type(type)
                        .reason(MovementReason.AJUSTEMENT)
                        .note("Ajustement suite à inventaire " + inventory.getReference())
                        .userEmail(userEmail)
                        .build();

                stockMovementRepository.save(movement);
            }
        }

        inventory.setStatus(InventoryStatus.VALIDE);
        inventory.setValidatedAt(LocalDateTime.now());
        return inventoryRepository.save(inventory);
    }

    @Override
    public Inventory getInventoryById(Long inventoryId) {
        return inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException("Inventaire non trouvé avec l'id: " + inventoryId));
    }

    @Override
    public List<Inventory> getAllInventories(Long boutiqueId) {
        if (boutiqueId != null) {
            return inventoryRepository.findByBoutiqueIdOrderByCreatedAtDesc(boutiqueId);
        }
        return inventoryRepository.findAllByOrderByCreatedAtDesc();
    }
}
