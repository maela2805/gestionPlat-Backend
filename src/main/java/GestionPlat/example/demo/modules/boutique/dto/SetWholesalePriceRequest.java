package GestionPlat.example.demo.modules.boutique.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SetWholesalePriceRequest {
    @NotNull(message = "L'ID du produit est obligatoire")
    private Long productId;

    @NotNull(message = "Le prix de cession est obligatoire")
    @PositiveOrZero(message = "Le prix de cession doit être positif ou nul")
    private BigDecimal wholesalePrice;

    private Boolean active;
}
