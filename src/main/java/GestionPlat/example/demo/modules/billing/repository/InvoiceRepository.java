package GestionPlat.example.demo.modules.billing.repository;

import GestionPlat.example.demo.modules.billing.model.Invoice;
import GestionPlat.example.demo.modules.billing.model.InvoiceStatus;
import GestionPlat.example.demo.modules.billing.model.InvoiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    List<Invoice> findByType(InvoiceType type);

    List<Invoice> findByStatus(InvoiceStatus status);

    List<Invoice> findByTypeAndStatus(InvoiceType type, InvoiceStatus status);

    List<Invoice> findByTiersId(Long tiersId);

    List<Invoice> findByBoutiqueId(Long boutiqueId);

    Optional<Invoice> findBySourceBoutiqueOrderId(Long sourceBoutiqueOrderId);

    boolean existsByInvoiceNumber(String invoiceNumber);

    @Query("SELECT SUM(i.remainingAmount) FROM Invoice i WHERE i.type = :type AND i.status IN ('VALIDEE', 'PAYEE_PARTIEL')")
    BigDecimal sumRemainingAmountByType(@Param("type") InvoiceType type);

    @Query("SELECT SUM(i.totalTtc) FROM Invoice i WHERE i.type = :type AND i.status IN ('VALIDEE', 'PAYEE_PARTIEL', 'PAYEE') AND i.invoiceDate BETWEEN :startDate AND :endDate")
    BigDecimal sumTotalTtcByTypeAndDateRange(@Param("type") InvoiceType type, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(i.totalTtc), 0) FROM Invoice i WHERE (i.boutique.id = :boutiqueId OR i.sourceBoutiqueOrder.boutique.id = :boutiqueId) AND i.type != 'ACHAT'")
    BigDecimal sumCessionInvoicesTotalTtcByBoutiqueId(@Param("boutiqueId") Long boutiqueId);

    @Query("SELECT i FROM Invoice i WHERE (i.boutique.id = :boutiqueId OR i.sourceBoutiqueOrder.boutique.id = :boutiqueId) AND i.type != 'ACHAT' AND i.remainingAmount > 0 ORDER BY i.invoiceDate ASC")
    List<Invoice> findUnpaidCessionInvoicesByBoutiqueId(@Param("boutiqueId") Long boutiqueId);

    @Query("SELECT COALESCE(SUM(i.remainingAmount), 0) FROM Invoice i WHERE (i.boutique.id = :boutiqueId OR i.sourceBoutiqueOrder.boutique.id = :boutiqueId) AND i.type != 'ACHAT'")
    BigDecimal sumCessionRemainingAmountByBoutiqueId(@Param("boutiqueId") Long boutiqueId);

    @Query("SELECT COALESCE(SUM(i.remainingAmount), 0) FROM Invoice i WHERE i.type != 'ACHAT'")
    BigDecimal sumGlobalRemainingAmount();
}
