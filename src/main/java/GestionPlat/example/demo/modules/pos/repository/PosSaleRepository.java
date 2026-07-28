package GestionPlat.example.demo.modules.pos.repository;

import GestionPlat.example.demo.modules.pos.model.PosSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PosSaleRepository extends JpaRepository<PosSale, Long> {
    List<PosSale> findByCashSessionIdOrderBySaleDateDesc(Long cashSessionId);
    List<PosSale> findByBoutiqueIdOrderBySaleDateDesc(Long boutiqueId);
    List<PosSale> findAllByOrderBySaleDateDesc();
    Optional<PosSale> findByReceiptNumber(String receiptNumber);
    boolean existsByReceiptNumber(String receiptNumber);
}
