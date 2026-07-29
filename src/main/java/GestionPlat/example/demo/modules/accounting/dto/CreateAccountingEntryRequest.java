package GestionPlat.example.demo.modules.accounting.dto;

import GestionPlat.example.demo.modules.accounting.model.AccountingCategory;
import GestionPlat.example.demo.modules.accounting.model.EntryType;
import GestionPlat.example.demo.modules.billing.model.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class CreateAccountingEntryRequest {
    private LocalDateTime entryDate;

    @NotNull(message = "Le type d'écriture est obligatoire (RECETTE/DEPENSE)")
    private EntryType type;

    @NotNull(message = "La catégorie comptable est obligatoire")
    private AccountingCategory category;

    @NotNull(message = "Le montant est obligatoire")
    @Positive(message = "Le montant doit être positif")
    private BigDecimal amount;

    private PaymentMethod paymentMethod;

    @NotBlank(message = "La description est obligatoire")
    private String description;

    private Long tiersId;
}
