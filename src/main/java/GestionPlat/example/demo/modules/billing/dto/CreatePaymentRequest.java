package GestionPlat.example.demo.modules.billing.dto;

import GestionPlat.example.demo.modules.billing.model.PaymentMethod;
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
public class CreatePaymentRequest {
    @NotNull(message = "L'ID de la facture est obligatoire")
    private Long invoiceId;

    @NotNull(message = "Le montant est obligatoire")
    @Positive(message = "Le montant doit être positif")
    private BigDecimal amount;

    @NotNull(message = "Le mode de paiement est obligatoire")
    private PaymentMethod paymentMethod;

    private LocalDateTime paymentDate;
    private String reference;
    private String note;
}
