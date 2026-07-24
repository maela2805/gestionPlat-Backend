package GestionPlat.example.demo.modules.sale.repository;

import GestionPlat.example.demo.modules.sale.model.StoreSale;
import GestionPlat.example.demo.modules.sale.model.StoreSaleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreSaleRepository extends JpaRepository<StoreSale, Long> {
    boolean existsByReference(String reference);
    List<StoreSale> findByBoutiqueId(Long boutiqueId);
    List<StoreSale> findByStatus(StoreSaleStatus status);
}
