package GestionPlat.example.demo.modules.sale.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateStoreSaleItemRequest {
    @NotNull(message = "L'ID du produit est obligatoire")
    private Long productId;

    private BigDecimal wholesalePrice; // Optional: custom wholesale price for this line, or auto-fetch if null

    @NotNull(message = "La quantité vendue est obligatoire")
    @Min(value = 1, message = "La quantité doit être d'au moins 1")
    private Integer quantity;
}
