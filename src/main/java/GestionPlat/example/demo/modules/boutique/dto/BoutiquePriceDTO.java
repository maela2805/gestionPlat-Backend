package GestionPlat.example.demo.modules.boutique.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoutiquePriceDTO {
    private Long id;
    private Long boutiqueId;
    private String boutiqueName;
    private Long productId;
    private String productReference;
    private String productName;
    private BigDecimal defaultBuyPrice;
    private BigDecimal defaultSellPrice;
    private BigDecimal wholesalePrice; // Prix de cession à cette boutique
    private Boolean active;
}
