package GestionPlat.example.demo.modules.accounting.repository;

import GestionPlat.example.demo.modules.accounting.model.AccountingCategory;
import GestionPlat.example.demo.modules.accounting.model.AccountingEntry;
import GestionPlat.example.demo.modules.accounting.model.EntryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AccountingEntryRepository extends JpaRepository<AccountingEntry, Long> {

    List<AccountingEntry> findByType(EntryType type);

    List<AccountingEntry> findByCategory(AccountingCategory category);

    List<AccountingEntry> findByEntryDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT SUM(e.amount) FROM AccountingEntry e WHERE e.type = :type AND e.entryDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByTypeAndDateRange(@Param("type") EntryType type, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(e.amount) FROM AccountingEntry e WHERE e.type = :type")
    BigDecimal sumAmountByType(@Param("type") EntryType type);
}
