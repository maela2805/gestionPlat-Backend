package GestionPlat.example.demo.modules.billing.dto;

import GestionPlat.example.demo.modules.billing.model.InvoiceType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateInvoiceRequest {
    @NotNull(message = "Le type de facture est obligatoire")
    private InvoiceType type;

    private LocalDateTime invoiceDate;
    private LocalDateTime dueDate;
    private Long tiersId;
    private Long boutiqueId;
    private Long sourceSaleId;
    private Long sourcePurchaseOrderId;
    private BigDecimal taxRate;
    private String note;
    private List<CreateInvoiceItemRequest> items;
}
