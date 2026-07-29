package GestionPlat.example.demo.modules.accounting.service;

import GestionPlat.example.demo.modules.accounting.dto.AccountingEntryDTO;
import GestionPlat.example.demo.modules.accounting.dto.CreateAccountingEntryRequest;
import GestionPlat.example.demo.modules.accounting.dto.FinancialSummaryDTO;
import GestionPlat.example.demo.modules.accounting.model.AccountingCategory;
import GestionPlat.example.demo.modules.accounting.model.EntryType;
import GestionPlat.example.demo.modules.billing.model.Invoice;
import GestionPlat.example.demo.modules.billing.model.Payment;

import java.time.LocalDateTime;
import java.util.List;

public interface AccountingService {

    List<AccountingEntryDTO> getAllEntries(EntryType type, AccountingCategory category, LocalDateTime startDate, LocalDateTime endDate);

    AccountingEntryDTO getEntryById(Long id);

    AccountingEntryDTO createManualEntry(CreateAccountingEntryRequest request);

    void recordPaymentAccountingEntry(Payment payment, Invoice invoice);

    FinancialSummaryDTO getFinancialSummary(LocalDateTime startDate, LocalDateTime endDate);
}
