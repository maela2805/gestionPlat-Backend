package GestionPlat.example.demo.modules.stock.repository;

import GestionPlat.example.demo.modules.stock.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByReference(String reference);
    Boolean existsByReference(String reference);

    Boolean existsByBarcode(String barcode);
    Boolean existsByBarcodeAndIdNot(String barcode, Long id);

    @Query("SELECT p FROM Product p WHERE p.stock <= p.alertThreshold")
    List<Product> findCriticalStockProducts();

    List<Product> findByCategoryId(Long categoryId);
    long countByCategoryId(Long categoryId);
}
