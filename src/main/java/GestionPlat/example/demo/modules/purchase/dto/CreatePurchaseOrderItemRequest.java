package GestionPlat.example.demo.modules.purchase.dto;

import jakarta.validation.constraints.Min;
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
public class CreatePurchaseOrderItemRequest {
    private Long productId;
    private String productName;
    private String productReference;
    private Long categoryId;

    @NotNull(message = "Le prix unitaire d'achat est obligatoire")
    @PositiveOrZero(message = "Le prix unitaire doit être positif")
    private BigDecimal unitPrice;

    @NotNull(message = "La quantité commandée est obligatoire")
    @Min(value = 1, message = "La quantité commandée doit être d'au moins 1")
    private Integer quantityOrdered;
}
