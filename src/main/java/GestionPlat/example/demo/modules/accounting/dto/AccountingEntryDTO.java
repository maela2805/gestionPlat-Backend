package GestionPlat.example.demo.modules.accounting.dto;

import GestionPlat.example.demo.modules.accounting.model.AccountingCategory;
import GestionPlat.example.demo.modules.accounting.model.EntryType;
import GestionPlat.example.demo.modules.billing.model.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountingEntryDTO {
    private Long id;
    private String entryCode;
    private LocalDateTime entryDate;
    private EntryType type;
    private AccountingCategory category;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private String description;
    private Long sourceInvoiceId;
    private String sourceInvoiceNumber;
    private Long sourceCashSessionId;
    private Long tiersId;
    private String tiersName;
    private LocalDateTime createdAt;
}
