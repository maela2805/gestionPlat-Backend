package GestionPlat.example.demo.modules.accounting.service.impl;

import GestionPlat.example.demo.modules.accounting.dto.AccountingEntryDTO;
import GestionPlat.example.demo.modules.accounting.dto.CreateAccountingEntryRequest;
import GestionPlat.example.demo.modules.accounting.dto.FinancialSummaryDTO;
import GestionPlat.example.demo.modules.accounting.model.AccountingCategory;
import GestionPlat.example.demo.modules.accounting.model.AccountingEntry;
import GestionPlat.example.demo.modules.accounting.model.EntryType;
import GestionPlat.example.demo.modules.accounting.repository.AccountingEntryRepository;
import GestionPlat.example.demo.modules.accounting.service.AccountingService;
import GestionPlat.example.demo.modules.billing.model.Invoice;
import GestionPlat.example.demo.modules.billing.model.InvoiceType;
import GestionPlat.example.demo.modules.billing.model.Payment;
import GestionPlat.example.demo.modules.billing.repository.InvoiceRepository;
import GestionPlat.example.demo.modules.tiers.model.Tiers;
import GestionPlat.example.demo.modules.tiers.repository.TiersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AccountingServiceImpl implements AccountingService {

    private final AccountingEntryRepository accountingEntryRepository;
    private final InvoiceRepository invoiceRepository;
    private final TiersRepository tiersRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AccountingEntryDTO> getAllEntries(EntryType type, AccountingCategory category, LocalDateTime startDate, LocalDateTime endDate) {
        List<AccountingEntry> list;
        if (startDate != null && endDate != null) {
            list = accountingEntryRepository.findByEntryDateBetween(startDate, endDate);
        } else if (type != null) {
            list = accountingEntryRepository.findByType(type);
        } else if (category != null) {
            list = accountingEntryRepository.findByCategory(category);
        } else {
            list = accountingEntryRepository.findAll();
        }

        if (type != null) {
            list = list.stream().filter(e -> e.getType() == type).toList();
        }
        if (category != null) {
            list = list.stream().filter(e -> e.getCategory() == category).toList();
        }

        return list.stream()
                .sorted(Comparator.comparing(AccountingEntry::getEntryDate).reversed())
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AccountingEntryDTO getEntryById(Long id) {
        AccountingEntry entry = accountingEntryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Écriture comptable introuvable avec l'ID : " + id));
        return mapToDTO(entry);
    }

    @Override
    @Transactional
    public AccountingEntryDTO createManualEntry(CreateAccountingEntryRequest request) {
        String code = "ECR-" + System.currentTimeMillis() % 1000000;

        Tiers tiers = null;
        if (request.getTiersId() != null) {
            tiers = tiersRepository.findById(request.getTiersId()).orElse(null);
        }

        LocalDateTime date = request.getEntryDate() != null ? request.getEntryDate() : LocalDateTime.now();

        AccountingEntry entry = AccountingEntry.builder()
                .entryCode(code)
                .entryDate(date)
                .type(request.getType())
                .category(request.getCategory())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .description(request.getDescription())
                .tiers(tiers)
                .build();

        AccountingEntry saved = accountingEntryRepository.save(entry);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public void recordPaymentAccountingEntry(Payment payment, Invoice invoice) {
        String code = "ECR-P-" + payment.getPaymentNumber();

        EntryType type = invoice.getType() == InvoiceType.VENTE ? EntryType.RECETTE : EntryType.DEPENSE;
        AccountingCategory category = invoice.getType() == InvoiceType.VENTE ? AccountingCategory.VENTES_PLATS : AccountingCategory.ACHATS_MATIERES;
        String desc = (type == EntryType.RECETTE ? "Règlement reçu - Facture N° " : "Règlement effectué - Facture N° ") + invoice.getInvoiceNumber();

        AccountingEntry entry = AccountingEntry.builder()
                .entryCode(code)
                .entryDate(payment.getPaymentDate() != null ? payment.getPaymentDate() : LocalDateTime.now())
                .type(type)
                .category(category)
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .description(desc)
                .sourceInvoice(invoice)
                .tiers(payment.getTiers() != null ? payment.getTiers() : invoice.getTiers())
                .build();

        accountingEntryRepository.save(entry);
    }

    @Override
    @Transactional(readOnly = true)
    public FinancialSummaryDTO getFinancialSummary(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null) {
            startDate = YearMonth.now().atDay(1).atStartOfDay();
        }
        if (endDate == null) {
            endDate = LocalDateTime.now().with(LocalTime.MAX);
        }

        List<AccountingEntry> periodEntries = accountingEntryRepository.findByEntryDateBetween(startDate, endDate);

        BigDecimal totalRevenue = periodEntries.stream()
                .filter(e -> e.getType() == EntryType.RECETTE)
                .map(AccountingEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = periodEntries.stream()
                .filter(e -> e.getType() == EntryType.DEPENSE)
                .map(AccountingEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netProfit = totalRevenue.subtract(totalExpense);

        BigDecimal clientReceivables = invoiceRepository.sumRemainingAmountByType(InvoiceType.VENTE);
        if (clientReceivables == null) clientReceivables = BigDecimal.ZERO;

        BigDecimal supplierPayables = invoiceRepository.sumRemainingAmountByType(InvoiceType.ACHAT);
        if (supplierPayables == null) supplierPayables = BigDecimal.ZERO;

        BigDecimal allRecettes = accountingEntryRepository.sumAmountByType(EntryType.RECETTE);
        if (allRecettes == null) allRecettes = BigDecimal.ZERO;

        BigDecimal allDepenses = accountingEntryRepository.sumAmountByType(EntryType.DEPENSE);
        if (allDepenses == null) allDepenses = BigDecimal.ZERO;

        BigDecimal cashBalance = allRecettes.subtract(allDepenses);

        Map<String, BigDecimal> expensesByCategory = new HashMap<>();
        for (AccountingEntry e : periodEntries) {
            if (e.getType() == EntryType.DEPENSE) {
                String cat = e.getCategory().name();
                expensesByCategory.put(cat, expensesByCategory.getOrDefault(cat, BigDecimal.ZERO).add(e.getAmount()));
            }
        }

        return FinancialSummaryDTO.builder()
                .totalRevenue(totalRevenue)
                .totalExpense(totalExpense)
                .netProfit(netProfit)
                .clientReceivables(clientReceivables)
                .supplierPayables(supplierPayables)
                .cashBalance(cashBalance)
                .expensesByCategory(expensesByCategory)
                .build();
    }

    private AccountingEntryDTO mapToDTO(AccountingEntry entry) {
        return AccountingEntryDTO.builder()
                .id(entry.getId())
                .entryCode(entry.getEntryCode())
                .entryDate(entry.getEntryDate())
                .type(entry.getType())
                .category(entry.getCategory())
                .amount(entry.getAmount())
                .paymentMethod(entry.getPaymentMethod())
                .description(entry.getDescription())
                .sourceInvoiceId(entry.getSourceInvoice() != null ? entry.getSourceInvoice().getId() : null)
                .sourceInvoiceNumber(entry.getSourceInvoice() != null ? entry.getSourceInvoice().getInvoiceNumber() : null)
                .sourceCashSessionId(entry.getSourceCashSession() != null ? entry.getSourceCashSession().getId() : null)
                .tiersId(entry.getTiers() != null ? entry.getTiers().getId() : null)
                .tiersName(entry.getTiers() != null ? entry.getTiers().getName() : null)
                .createdAt(entry.getCreatedAt())
                .build();
    }
}
