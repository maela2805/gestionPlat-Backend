package GestionPlat.example.demo.modules.billing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateInvoiceItemRequest {
    @NotBlank(message = "La description de l'article est obligatoire")
    private String description;

    @NotNull(message = "La quantité est obligatoire")
    @Positive(message = "La quantité doit être supérieure à zéro")
    private BigDecimal quantity;

    @NotNull(message = "Le prix unitaire HT est obligatoire")
    private BigDecimal unitPriceHt;

    private BigDecimal taxRate;
}
