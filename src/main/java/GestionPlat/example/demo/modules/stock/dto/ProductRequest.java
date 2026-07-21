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
public class ProductRequest {
    private String reference;
    private String name;
    private String description;
    private BigDecimal buyPrice;
    private BigDecimal sellPrice;
    private Integer initialStock;
    private Integer alertThreshold;
    private String barcode;
    private String imageUrl;
    private Long categoryId;
}
