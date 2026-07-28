package GestionPlat.example.demo.modules.caisse.repository;

import GestionPlat.example.demo.modules.caisse.model.CashSession;
import GestionPlat.example.demo.modules.caisse.model.CashSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CashSessionRepository extends JpaRepository<CashSession, Long> {
    Optional<CashSession> findByBoutiqueIdAndStatus(Long boutiqueId, CashSessionStatus status);
    Optional<CashSession> findByUserEmailAndStatus(String userEmail, CashSessionStatus status);
    List<CashSession> findByBoutiqueIdOrderByOpeningDateDesc(Long boutiqueId);
    List<CashSession> findAllByOrderByOpeningDateDesc();
    boolean existsByBoutiqueIdAndStatus(Long boutiqueId, CashSessionStatus status);
    boolean existsBySessionReference(String sessionReference);
}
