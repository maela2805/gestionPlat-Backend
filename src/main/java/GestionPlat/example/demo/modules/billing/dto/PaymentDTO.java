package GestionPlat.example.demo.modules.billing.dto;

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
public class PaymentDTO {
    private Long id;
    private String paymentNumber;
    private Long invoiceId;
    private String invoiceNumber;
    private Long tiersId;
    private String tiersName;
    private LocalDateTime paymentDate;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private String reference;
    private String note;
    private LocalDateTime createdAt;
}
