package GestionPlat.example.demo.modules.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoutiqueStockDTO {
    private Long id;
    private Long boutiqueId;
    private String boutiqueName;
    private Long productId;
    private String productName;
    private String productReference;
    private Integer quantity;
    private BigDecimal buyPrice;
    private BigDecimal sellPrice;
    private Integer alertThreshold;
    private Long categoryId;
    private String categoryName;
    private String imageUrl;
}
