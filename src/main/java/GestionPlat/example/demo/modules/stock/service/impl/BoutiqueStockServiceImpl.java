package GestionPlat.example.demo.modules.stock.service.impl;

import GestionPlat.example.demo.modules.boutique.model.Boutique;
import GestionPlat.example.demo.modules.boutique.repository.BoutiqueRepository;
import GestionPlat.example.demo.modules.stock.dto.BoutiqueStockDTO;
import GestionPlat.example.demo.modules.stock.dto.TransferStockRequest;
import GestionPlat.example.demo.modules.stock.model.BoutiqueStock;
import GestionPlat.example.demo.modules.stock.model.Product;
import GestionPlat.example.demo.modules.stock.model.StockMovement;
import GestionPlat.example.demo.modules.stock.model.StockMovement.MovementReason;
import GestionPlat.example.demo.modules.stock.model.StockMovement.MovementType;
import GestionPlat.example.demo.modules.stock.repository.BoutiqueStockRepository;
import GestionPlat.example.demo.modules.stock.repository.ProductRepository;
import GestionPlat.example.demo.modules.stock.repository.StockMovementRepository;
import GestionPlat.example.demo.modules.stock.service.BoutiqueStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BoutiqueStockServiceImpl implements BoutiqueStockService {

    private final BoutiqueStockRepository boutiqueStockRepository;
    private final ProductRepository productRepository;
    private final BoutiqueRepository boutiqueRepository;
    private final StockMovementRepository stockMovementRepository;

    @Override
    public List<BoutiqueStockDTO> getStocksByBoutique(Long boutiqueId) {
        Boutique boutique = boutiqueRepository.findById(boutiqueId)
                .orElseThrow(() -> new RuntimeException("Boutique non trouvée avec l'id: " + boutiqueId));

        List<Product> products = productRepository.findAll();
        List<BoutiqueStockDTO> dtoList = new ArrayList<>();

        for (Product product : products) {
            Optional<BoutiqueStock> bStockOpt = boutiqueStockRepository.findByBoutiqueIdAndProductId(boutiqueId, product.getId());
            int qty = bStockOpt.map(BoutiqueStock::getQuantity).orElse(0);

            dtoList.add(BoutiqueStockDTO.builder()
                    .id(bStockOpt.map(BoutiqueStock::getId).orElse(null))
                    .boutiqueId(boutique.getId())
                    .boutiqueName(boutique.getName())
                    .productId(product.getId())
                    .productName(product.getName())
                    .productReference(product.getReference())
                    .quantity(qty)
                    .buyPrice(product.getBuyPrice())
                    .sellPrice(product.getSellPrice())
                    .alertThreshold(product.getAlertThreshold())
                    .build());
        }

        return dtoList;
    }

    @Override
    public List<BoutiqueStockDTO> getAllBoutiqueStocks() {
        List<BoutiqueStock> stocks = boutiqueStockRepository.findAll();
        List<BoutiqueStockDTO> dtoList = new ArrayList<>();
        for (BoutiqueStock bs : stocks) {
            dtoList.add(BoutiqueStockDTO.builder()
                    .id(bs.getId())
                    .boutiqueId(bs.getBoutique().getId())
                    .boutiqueName(bs.getBoutique().getName())
                    .productId(bs.getProduct().getId())
                    .productName(bs.getProduct().getName())
                    .productReference(bs.getProduct().getReference())
                    .quantity(bs.getQuantity())
                    .buyPrice(bs.getProduct().getBuyPrice())
                    .sellPrice(bs.getProduct().getSellPrice())
                    .alertThreshold(bs.getProduct().getAlertThreshold())
                    .build());
        }
        return dtoList;
    }

    @Override
    @Transactional
    public void transferStock(TransferStockRequest request, String userEmail) {
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new RuntimeException("La quantité de transfert doit être supérieure à zéro.");
        }

        if (Objects.equals(request.getFromBoutiqueId(), request.getToBoutiqueId())) {
            throw new RuntimeException("L'entrepôt de départ et de destination doivent être différents.");
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'id: " + request.getProductId()));

        int qty = request.getQuantity();

        // 1. Déduction à la source
        Boutique sourceBoutique = null;
        if (request.getFromBoutiqueId() == null) {
            // Source = Entrepôt Central
            int currentCentralStock = product.getStock() != null ? product.getStock() : 0;
            if (currentCentralStock < qty) {
                throw new RuntimeException("Stock insuffisant dans l'entrepôt central. Disponible: " + currentCentralStock);
            }
            product.setStock(currentCentralStock - qty);
            productRepository.save(product);
        } else {
            // Source = Entrepôt d'une boutique
            sourceBoutique = boutiqueRepository.findById(request.getFromBoutiqueId())
                    .orElseThrow(() -> new RuntimeException("Boutique source non trouvée."));
            BoutiqueStock sourceStock = boutiqueStockRepository.findByBoutiqueIdAndProductId(request.getFromBoutiqueId(), product.getId())
                    .orElseThrow(() -> new RuntimeException("Aucun stock trouvé dans la boutique source pour ce produit."));

            int currentBoutiqueStock = sourceStock.getQuantity() != null ? sourceStock.getQuantity() : 0;
            if (currentBoutiqueStock < qty) {
                throw new RuntimeException("Stock insuffisant dans " + sourceBoutique.getName() + ". Disponible: " + currentBoutiqueStock);
            }
            sourceStock.setQuantity(currentBoutiqueStock - qty);
            boutiqueStockRepository.save(sourceStock);
        }

        // 2. Ajout à la destination
        Boutique targetBoutique = null;
        if (request.getToBoutiqueId() == null) {
            // Destination = Entrepôt Central
            int currentCentralStock = product.getStock() != null ? product.getStock() : 0;
            product.setStock(currentCentralStock + qty);
            productRepository.save(product);
        } else {
            // Destination = Entrepôt d'une boutique
            targetBoutique = boutiqueRepository.findById(request.getToBoutiqueId())
                    .orElseThrow(() -> new RuntimeException("Boutique destination non trouvée."));
            final Boutique finalTargetBoutique = targetBoutique;

            BoutiqueStock targetStock = boutiqueStockRepository.findByBoutiqueIdAndProductId(request.getToBoutiqueId(), product.getId())
                    .orElseGet(() -> BoutiqueStock.builder()
                            .boutique(finalTargetBoutique)
                            .product(product)
                            .quantity(0)
                            .build());

            targetStock.setQuantity(targetStock.getQuantity() + qty);
            boutiqueStockRepository.save(targetStock);
        }

        // 3. Traçabilité des mouvements (SORTIE à la source & ENTREE à la destination)
        String fromName = sourceBoutique != null ? sourceBoutique.getName() : "Entrepôt Central";
        String toName = targetBoutique != null ? targetBoutique.getName() : "Entrepôt Central";

        StockMovement exitMovement = StockMovement.builder()
                .product(product)
                .boutique(sourceBoutique)
                .quantity(qty)
                .type(MovementType.SORTIE)
                .reason(MovementReason.REAPPROVISIONNEMENT)
                .note("Transfert sortant vers " + toName + ". " + (request.getNote() != null ? request.getNote() : ""))
                .userEmail(userEmail)
                .build();
        stockMovementRepository.save(exitMovement);

        StockMovement entryMovement = StockMovement.builder()
                .product(product)
                .boutique(targetBoutique)
                .quantity(qty)
                .type(MovementType.ENTREE)
                .reason(MovementReason.REAPPROVISIONNEMENT)
                .note("Transfert entrant depuis " + fromName + ". " + (request.getNote() != null ? request.getNote() : ""))
                .userEmail(userEmail)
                .build();
        stockMovementRepository.save(entryMovement);
    }
}
