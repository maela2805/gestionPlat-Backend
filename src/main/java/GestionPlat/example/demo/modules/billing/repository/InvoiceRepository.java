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

    @Query(value = "SELECT COALESCE(SUM(i.total_ttc), 0) FROM invoices i LEFT JOIN boutique_orders bo ON bo.id = i.source_boutique_order_id WHERE (i.boutique_id = :boutiqueId OR bo.boutique_id = :boutiqueId) AND (i.type IS NULL OR i.type != 'ACHAT')", nativeQuery = true)
    BigDecimal sumCessionInvoicesTotalTtcByBoutiqueId(@Param("boutiqueId") Long boutiqueId);

    @Query("SELECT i FROM Invoice i LEFT JOIN i.sourceBoutiqueOrder bo WHERE (i.boutique.id = :boutiqueId OR bo.boutique.id = :boutiqueId) AND (i.type IS NULL OR i.type != GestionPlat.example.demo.modules.billing.model.InvoiceType.ACHAT) AND (i.remainingAmount IS NULL OR i.remainingAmount > 0) ORDER BY i.invoiceDate ASC")
    List<Invoice> findUnpaidCessionInvoicesByBoutiqueId(@Param("boutiqueId") Long boutiqueId);

    @Query(value = "SELECT COALESCE(SUM(i.remaining_amount), 0) FROM invoices i LEFT JOIN boutique_orders bo ON bo.id = i.source_boutique_order_id WHERE (i.boutique_id = :boutiqueId OR bo.boutique_id = :boutiqueId) AND (i.type IS NULL OR i.type != 'ACHAT')", nativeQuery = true)
    BigDecimal sumCessionRemainingAmountByBoutiqueId(@Param("boutiqueId") Long boutiqueId);

    @Query(value = "SELECT COALESCE(SUM(remaining_amount), 0) FROM invoices WHERE (type IS NULL OR type != 'ACHAT')", nativeQuery = true)
    BigDecimal sumGlobalRemainingAmount();

    @Query(value = "SELECT COALESCE(SUM(i.total_ttc), 0) FROM invoices i WHERE (i.type IS NULL OR i.type != 'ACHAT')", nativeQuery = true)
    BigDecimal sumAllCessionInvoicesTotalTtc();
}
