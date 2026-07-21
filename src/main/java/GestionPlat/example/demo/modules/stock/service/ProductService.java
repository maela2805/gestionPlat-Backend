package GestionPlat.example.demo.modules.stock.service;

import GestionPlat.example.demo.modules.stock.dto.ProductRequest;
import GestionPlat.example.demo.modules.stock.model.Product;

import java.util.List;

public interface ProductService {
    List<Product> getAllProducts();
    Product getProductById(Long id);
    List<Product> getCriticalStockProducts();
    Product createProduct(ProductRequest request, String userEmail);
    Product updateProduct(Long id, ProductRequest request);
    void deleteProduct(Long id);
}
