package GestionPlat.example.demo.modules.stock.repository;

import GestionPlat.example.demo.modules.stock.model.BoutiqueStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoutiqueStockRepository extends JpaRepository<BoutiqueStock, Long> {
    List<BoutiqueStock> findByBoutiqueId(Long boutiqueId);
    Optional<BoutiqueStock> findByBoutiqueIdAndProductId(Long boutiqueId, Long productId);
    List<BoutiqueStock> findByProductId(Long productId);
}
