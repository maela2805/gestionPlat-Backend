package GestionPlat.example.demo.modules.accounting.controller;

import GestionPlat.example.demo.modules.accounting.dto.AccountingEntryDTO;
import GestionPlat.example.demo.modules.accounting.dto.CreateAccountingEntryRequest;
import GestionPlat.example.demo.modules.accounting.dto.FinancialSummaryDTO;
import GestionPlat.example.demo.modules.accounting.model.AccountingCategory;
import GestionPlat.example.demo.modules.accounting.model.EntryType;
import GestionPlat.example.demo.modules.accounting.service.AccountingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/accounting")
@RequiredArgsConstructor
public class AccountingController {

    private final AccountingService accountingService;

    @GetMapping("/entries")
    public ResponseEntity<List<AccountingEntryDTO>> getAllEntries(
            @RequestParam(required = false) EntryType type,
            @RequestParam(required = false) AccountingCategory category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(accountingService.getAllEntries(type, category, startDate, endDate));
    }

    @GetMapping("/entries/{id}")
    public ResponseEntity<AccountingEntryDTO> getEntryById(@PathVariable Long id) {
        return ResponseEntity.ok(accountingService.getEntryById(id));
    }

    @PostMapping("/entries")
    public ResponseEntity<AccountingEntryDTO> createManualEntry(@Valid @RequestBody CreateAccountingEntryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountingService.createManualEntry(request));
    }

    @GetMapping("/summary")
    public ResponseEntity<FinancialSummaryDTO> getFinancialSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(accountingService.getFinancialSummary(startDate, endDate));
    }
}
