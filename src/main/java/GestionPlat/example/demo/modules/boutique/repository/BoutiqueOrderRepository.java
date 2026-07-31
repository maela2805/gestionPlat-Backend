package GestionPlat.example.demo.modules.boutique.repository;

import GestionPlat.example.demo.modules.boutique.model.BoutiqueOrder;
import GestionPlat.example.demo.modules.boutique.model.BoutiqueOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoutiqueOrderRepository extends JpaRepository<BoutiqueOrder, Long> {
    List<BoutiqueOrder> findByBoutiqueIdOrderByCreatedAtDesc(Long boutiqueId);
    List<BoutiqueOrder> findAllByOrderByCreatedAtDesc();
    Optional<BoutiqueOrder> findByOrderNumber(String orderNumber);
    List<BoutiqueOrder> findByStatus(BoutiqueOrderStatus status);
}
