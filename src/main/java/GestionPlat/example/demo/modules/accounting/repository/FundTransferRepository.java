package GestionPlat.example.demo.modules.accounting.repository;

import GestionPlat.example.demo.modules.accounting.model.FundTransfer;
import GestionPlat.example.demo.modules.accounting.model.FundTransferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface FundTransferRepository extends JpaRepository<FundTransfer, Long> {

    List<FundTransfer> findByBoutiqueIdOrderByCreatedAtDesc(Long boutiqueId);

    List<FundTransfer> findByStatusOrderByCreatedAtDesc(FundTransferStatus status);

    List<FundTransfer> findAllByOrderByCreatedAtDesc();

    @Query("SELECT COALESCE(SUM(ft.amount), 0) FROM FundTransfer ft WHERE ft.boutique.id = :boutiqueId AND ft.status = :status")
    BigDecimal sumAmountByBoutiqueIdAndStatus(@Param("boutiqueId") Long boutiqueId, @Param("status") FundTransferStatus status);

    @Query("SELECT COALESCE(SUM(ft.amount), 0) FROM FundTransfer ft WHERE ft.cashSession.id = :sessionId AND ft.status != 'REJECTED' AND ft.status != 'CANCELLED'")
    BigDecimal sumAmountByCashSessionId(@Param("sessionId") Long sessionId);

    @Query("SELECT COALESCE(SUM(ft.amount), 0) FROM FundTransfer ft WHERE ft.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") FundTransferStatus status);

    @Query("SELECT COALESCE(SUM(ft.amount), 0) FROM FundTransfer ft WHERE ft.boutique.id = :boutiqueId AND ft.status != 'REJECTED' AND ft.status != 'CANCELLED'")
    BigDecimal sumTransferredAmountByBoutiqueId(@Param("boutiqueId") Long boutiqueId);

    @Query("SELECT COALESCE(SUM(ft.amount), 0) FROM FundTransfer ft WHERE ft.status != 'REJECTED' AND ft.status != 'CANCELLED'")
    BigDecimal sumAllTransferredAmount();
}
