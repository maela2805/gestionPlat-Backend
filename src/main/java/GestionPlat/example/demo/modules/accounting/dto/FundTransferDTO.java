package GestionPlat.example.demo.modules.accounting.dto;

import GestionPlat.example.demo.modules.accounting.model.FundTransferStatus;
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
public class FundTransferDTO {
    private Long id;
    private String reference;
    private Long boutiqueId;
    private String boutiqueName;
    private Long cashSessionId;
    private GestionPlat.example.demo.modules.accounting.model.VersementType versemenType;
    private Long invoiceId;
    private String invoiceNumber;
    private BigDecimal invoiceTotalAmount;
    private BigDecimal invoiceRemainingAmount;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private String proofUrl;
    private String userEmail;
    private String approvedByEmail;
    private FundTransferStatus status;
    private String notes;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
