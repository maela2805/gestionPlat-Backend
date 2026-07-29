package GestionPlat.example.demo.modules.billing.dto;

import GestionPlat.example.demo.modules.billing.model.InvoiceStatus;
import GestionPlat.example.demo.modules.billing.model.InvoiceType;
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
public class InvoiceDTO {
    private Long id;
    private String invoiceNumber;
    private InvoiceType type;
    private InvoiceStatus status;
    private LocalDateTime invoiceDate;
    private LocalDateTime dueDate;
    private Long tiersId;
    private String tiersName;
    private String tiersCode;
    private Long boutiqueId;
    private String boutiqueName;
    private Long sourceSaleId;
    private Long sourcePurchaseOrderId;
    private BigDecimal subtotalHt;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal totalTtc;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;
    private String note;
    private List<InvoiceItemDTO> items;
    private List<PaymentDTO> payments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
