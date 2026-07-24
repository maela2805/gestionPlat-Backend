package GestionPlat.example.demo.modules.sale.repository;

import GestionPlat.example.demo.modules.sale.model.StoreSaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreSaleItemRepository extends JpaRepository<StoreSaleItem, Long> {
    List<StoreSaleItem> findByStoreSaleId(Long storeSaleId);
}
