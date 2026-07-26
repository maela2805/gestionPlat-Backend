package GestionPlat.example.demo.modules.stock.repository;

import GestionPlat.example.demo.modules.stock.model.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findByProductIdOrderByCreatedAtDesc(Long productId);
    List<StockMovement> findByBoutiqueIdOrderByCreatedAtDesc(Long boutiqueId);
    List<StockMovement> findAllByOrderByCreatedAtDesc();
}
