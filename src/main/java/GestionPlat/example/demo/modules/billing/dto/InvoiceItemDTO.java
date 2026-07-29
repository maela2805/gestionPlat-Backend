package GestionPlat.example.demo.modules.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceItemDTO {
    private Long id;
    private String description;
    private BigDecimal quantity;
    private BigDecimal unitPriceHt;
    private BigDecimal taxRate;
    private BigDecimal totalHt;
    private BigDecimal totalTtc;
}
