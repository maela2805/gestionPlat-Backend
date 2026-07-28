package GestionPlat.example.demo.modules.pos.dto;

import GestionPlat.example.demo.modules.pos.model.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePosSaleRequest {
    private Long boutiqueId;
    private Long cashSessionId;
    private Long clientId;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal amountPaid;
    private PaymentMethod paymentMethod;
    private String notes;
    private List<CreatePosSaleItemRequest> items;
}
