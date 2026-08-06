package GestionPlat.example.demo.modules.pos.repository;

import GestionPlat.example.demo.modules.pos.model.PosSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PosSaleRepository extends JpaRepository<PosSale, Long> {
    List<PosSale> findByCashSessionIdOrderBySaleDateDesc(Long cashSessionId);
    List<PosSale> findByBoutiqueIdOrderBySaleDateDesc(Long boutiqueId);
    List<PosSale> findAllByOrderBySaleDateDesc();
    Optional<PosSale> findByReceiptNumber(String receiptNumber);
    boolean existsByReceiptNumber(String receiptNumber);

    @Query("SELECT COALESCE(SUM(s.netAmount), 0) FROM PosSale s WHERE s.boutique.id = :boutiqueId AND s.status != 'CANCELEE' AND s.paymentMethod = 'ESPECES'")
    BigDecimal sumCashSalesByBoutiqueId(@Param("boutiqueId") Long boutiqueId);

    @Query("SELECT COALESCE(SUM(s.netAmount), 0) FROM PosSale s WHERE s.status != 'CANCELEE' AND s.paymentMethod = 'ESPECES'")
    BigDecimal sumAllCashSales();
}
