package GestionPlat.example.demo.modules.caisse.repository;

import GestionPlat.example.demo.modules.caisse.model.CashMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CashMovementRepository extends JpaRepository<CashMovement, Long> {
    List<CashMovement> findByCashSessionIdOrderByCreatedAtDesc(Long cashSessionId);
}
