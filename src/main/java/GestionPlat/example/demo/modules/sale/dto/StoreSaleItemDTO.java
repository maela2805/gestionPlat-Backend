package GestionPlat.example.demo.modules.sale.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreSaleItemDTO {
    private Long id;
    private Long productId;
    private String productReference;
    private String productName;
    private BigDecimal wholesalePrice;
    private Integer quantity;
    private BigDecimal totalPrice;
}
