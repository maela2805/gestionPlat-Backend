package GestionPlat.example.demo.modules.stock.service.impl;

import GestionPlat.example.demo.modules.stock.dto.ProductRequest;
import GestionPlat.example.demo.modules.stock.model.Category;
import GestionPlat.example.demo.modules.stock.model.Product;
import GestionPlat.example.demo.modules.stock.model.StockMovement;
import GestionPlat.example.demo.modules.stock.model.StockMovement.MovementReason;
import GestionPlat.example.demo.modules.stock.model.StockMovement.MovementType;
import GestionPlat.example.demo.modules.stock.repository.CategoryRepository;
import GestionPlat.example.demo.modules.stock.repository.ProductRepository;
import GestionPlat.example.demo.modules.stock.repository.StockMovementRepository;
import GestionPlat.example.demo.modules.stock.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StockMovementRepository stockMovementRepository;

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'id: " + id));
    }

    @Override
    public List<Product> getCriticalStockProducts() {
        return productRepository.findCriticalStockProducts();
    }

    @Override
    @Transactional
    public Product createProduct(ProductRequest request, String userEmail) {
        String reference = request.getReference();
        if (reference == null || reference.isBlank()) {
            long count = productRepository.count() + 1;
            reference = String.format("PROD-%05d", count);
        }

        if (productRepository.existsByReference(reference)) {
            long count = productRepository.count() + 1;
            reference = String.format("PROD-%05d", count);
            if (productRepository.existsByReference(reference)) {
                reference = "PROD-" + System.currentTimeMillis() % 100000;
            }
        }

        if (request.getBarcode() != null && !request.getBarcode().isBlank()) {
            String trimmedBarcode = request.getBarcode().trim();
            if (productRepository.existsByBarcode(trimmedBarcode)) {
                throw new RuntimeException(
                        "Le code-barres '" + trimmedBarcode + "' est déjà attribué à un autre produit !");
            }
        }

        if (request.getSellPrice().compareTo(request.getBuyPrice()) < 0) {
            throw new RuntimeException(
                    "Règle de gestion : Le prix de vente doit être supérieur ou égal au prix d'achat.");
        }

        Category category = null;
        if (request.getCategoryId() != null) {
            Category selectedCategory = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(
                            () -> new RuntimeException("Catégorie non trouvée avec l'id: " + request.getCategoryId()));
            category = resolveEffectiveCategory(selectedCategory);
        }

        int initialStock = request.getInitialStock() != null ? request.getInitialStock() : 0;

        Product product = Product.builder()
                .reference(reference)
                .name(request.getName())
                .description(request.getDescription())
                .buyPrice(request.getBuyPrice())
                .sellPrice(request.getSellPrice())
                .stock(initialStock)
                .alertThreshold(request.getAlertThreshold() != null ? request.getAlertThreshold() : 5)
                .barcode(request.getBarcode() != null ? request.getBarcode().trim() : null)
                .imageUrl(request.getImageUrl())
                .category(category)
                .build();

        Product savedProduct = productRepository.save(product);

        if (initialStock > 0) {
            StockMovement movement = StockMovement.builder()
                    .product(savedProduct)
                    .quantity(initialStock)
                    .type(MovementType.ENTREE)
                    .reason(MovementReason.REAPPROVISIONNEMENT)
                    .note("Stock initial lors de la création du produit")
                    .userEmail(userEmail)
                    .build();
            stockMovementRepository.save(movement);
        }

        return savedProduct;
    }

    @Override
    @Transactional
    public Product updateProduct(Long id, ProductRequest request) {
        Product product = getProductById(id);

        if (request.getBarcode() != null && !request.getBarcode().isBlank()) {
            String trimmedBarcode = request.getBarcode().trim();
            if (productRepository.existsByBarcodeAndIdNot(trimmedBarcode, id)) {
                throw new RuntimeException(
                        "Le code-barres '" + trimmedBarcode + "' est déjà attribué à un autre produit !");
            }
        }

        if (request.getSellPrice().compareTo(request.getBuyPrice()) < 0) {
            throw new RuntimeException(
                    "Règle de gestion : Le prix de vente doit être supérieur ou égal au prix d'achat.");
        }

        if (request.getCategoryId() != null) {
            Category selectedCategory = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Catégorie non trouvée"));
            product.setCategory(resolveEffectiveCategory(selectedCategory));
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setBuyPrice(request.getBuyPrice());
        product.setSellPrice(request.getSellPrice());
        if (request.getAlertThreshold() != null)
            product.setAlertThreshold(request.getAlertThreshold());
        if (request.getBarcode() != null)
            product.setBarcode(request.getBarcode().trim());
        if (request.getImageUrl() != null)
            product.setImageUrl(request.getImageUrl());

        return productRepository.save(product);
    }

    Category resolveEffectiveCategory(Category category) {
        if (category == null) {
            return null;
        }

        Category current = category;
        while (current.getParent() != null) {
            current = current.getParent();
        }
        return current;
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        List<StockMovement> movements = stockMovementRepository.findByProductIdOrderByCreatedAtDesc(id);

        if (!movements.isEmpty()) {
            throw new RuntimeException(
                    "Règle de gestion : Impossible de supprimer un produit ayant un historique de mouvements de stock.");
        }

        productRepository.delete(product);
    }
}
