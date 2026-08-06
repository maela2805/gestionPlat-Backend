package GestionPlat.example.demo.modules.accounting.dto;

import GestionPlat.example.demo.modules.billing.model.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateFundTransferRequest {

    private Long boutiqueId;
    private Long cashSessionId;
    private GestionPlat.example.demo.modules.accounting.model.VersementType versemenType;
    private Long invoiceId;

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant doit être supérieur à 0")
    private BigDecimal amount;

    @NotNull(message = "Le moyen de paiement est obligatoire")
    private PaymentMethod paymentMethod;

    private String proofUrl;
    private String notes;
}
