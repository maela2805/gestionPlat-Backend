package GestionPlat.example.demo.modules.purchase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderItemDTO {
    private Long id;
    private Long productId;
    private String productReference;
    private String productName;
    private BigDecimal unitPrice;
    private Integer quantityOrdered;
    private Integer quantityReceived;
    private BigDecimal totalPrice;
    private Boolean isPendingProduct;
}
