package GestionPlat.example.demo.modules.stock.controller;

import GestionPlat.example.demo.modules.stock.dto.ProductRequest;
import GestionPlat.example.demo.modules.stock.dto.StockAdjustmentRequest;
import GestionPlat.example.demo.modules.stock.model.Product;
import GestionPlat.example.demo.modules.stock.service.ProductService;
import GestionPlat.example.demo.modules.stock.service.StockMovementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock/products")
@RequiredArgsConstructor
@Tag(name = "2. Gestion de Stock - Produits", description = "CRUD Produits, ajustements de stock et alertes de stock critique")
public class ProductController {

    private final ProductService productService;
    private final StockMovementService stockMovementService;

    @Operation(summary = "Liste de tous les produits")
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @Operation(summary = "Obtenir un produit par son ID")
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @Operation(summary = "Obtenir la liste des produits en alerte de stock (stock <= seuil)")
    @GetMapping("/alerts")
    public ResponseEntity<List<Product>> getCriticalStockProducts() {
        return ResponseEntity.ok(productService.getCriticalStockProducts());
    }

    @Operation(summary = "Créer un nouveau produit (ADMIN/MANAGER)")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER', 'WRITE_STOCK')")
    public ResponseEntity<Product> createProduct(@RequestBody ProductRequest request,
                                                 @AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = userDetails != null ? userDetails.getUsername() : "system";
        return ResponseEntity.ok(productService.createProduct(request, userEmail));
    }

    @Operation(summary = "Mettre à jour un produit existant (ADMIN/MANAGER)")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER', 'WRITE_STOCK')")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @Operation(summary = "Supprimer un produit (ADMIN uniquement)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'DELETE_STOCK')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Ajuster la quantité en stock d'un produit (Entrée/Sortie)")
    @PostMapping("/adjust")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER', 'WRITE_STOCK')")
    public ResponseEntity<Product> adjustStock(@RequestBody StockAdjustmentRequest request,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = userDetails != null ? userDetails.getUsername() : "system";
        return ResponseEntity.ok(stockMovementService.adjustStock(request, userEmail));
    }
}
