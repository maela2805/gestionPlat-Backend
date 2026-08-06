package GestionPlat.example.demo.modules.boutique.dto;

import GestionPlat.example.demo.modules.boutique.model.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoutiquePromotionDTO {
    private Long id;
    private String name;
    private Long boutiqueId;
    private String boutiqueName;
    private Long productId;
    private String productName;
    private String productReference;
    private Double originalPrice;
    private Double promoPrice;
    private DiscountType discountType;
    private Double discountValue;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean active;
    private boolean currentlyActive;
}
