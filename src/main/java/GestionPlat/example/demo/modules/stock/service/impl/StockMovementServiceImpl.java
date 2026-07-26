package GestionPlat.example.demo.modules.stock.service.impl;

import GestionPlat.example.demo.modules.stock.dto.StockAdjustmentRequest;
import GestionPlat.example.demo.modules.stock.model.Product;
import GestionPlat.example.demo.modules.stock.model.StockMovement;
import GestionPlat.example.demo.modules.stock.model.StockMovement.MovementType;
import GestionPlat.example.demo.modules.stock.repository.ProductRepository;
import GestionPlat.example.demo.modules.stock.repository.StockMovementRepository;
import GestionPlat.example.demo.modules.stock.service.StockMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockMovementServiceImpl implements StockMovementService {

    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;

    @Override
    @Transactional
    public Product adjustStock(StockAdjustmentRequest request, String userEmail) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'id: " + request.getProductId()));

        int qty = Math.abs(request.getQuantity());

        if (request.getType() == MovementType.SORTIE) {
            if (product.getStock() < qty) {
                throw new RuntimeException("Règle de gestion : Le stock ne peut pas être négatif ! Stock actuel : " + product.getStock());
            }
            product.setStock(product.getStock() - qty);
        } else {
            product.setStock(product.getStock() + qty);
        }

        Product updatedProduct = productRepository.save(product);

        StockMovement movement = StockMovement.builder()
                .product(updatedProduct)
                .quantity(qty)
                .type(request.getType())
                .reason(request.getReason())
                .note(request.getNote())
                .userEmail(userEmail)
                .build();

        stockMovementRepository.save(movement);

        return updatedProduct;
    }

    @Override
    public List<StockMovement> getAllStockMovements() {
        return stockMovementRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public List<StockMovement> getMovementsByProduct(Long productId) {
        return stockMovementRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }

    @Override
    public List<StockMovement> getMovementsByBoutique(Long boutiqueId) {
        return stockMovementRepository.findByBoutiqueIdOrderByCreatedAtDesc(boutiqueId);
    }
}
