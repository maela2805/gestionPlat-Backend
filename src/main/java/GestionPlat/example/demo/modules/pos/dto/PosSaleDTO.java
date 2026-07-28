package GestionPlat.example.demo.modules.pos.dto;

import GestionPlat.example.demo.modules.pos.model.PaymentMethod;
import GestionPlat.example.demo.modules.pos.model.PosSaleStatus;
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
public class PosSaleDTO {
    private Long id;
    private String receiptNumber;
    private Long cashSessionId;
    private String sessionReference;
    private Long boutiqueId;
    private String boutiqueCode;
    private String boutiqueName;
    private Long clientId;
    private String customClientName;
    private String clientName;
    private LocalDateTime saleDate;
    private BigDecimal subTotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal netAmount;
    private BigDecimal amountPaid;
    private BigDecimal changeReturned;
    private PaymentMethod paymentMethod;
    private PosSaleStatus status;
    private String userEmail;
    private String notes;
    private List<PosSaleItemDTO> items;
    private LocalDateTime createdAt;
}
