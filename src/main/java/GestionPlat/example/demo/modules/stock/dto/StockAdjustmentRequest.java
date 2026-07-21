package GestionPlat.example.demo.modules.stock.dto;

import GestionPlat.example.demo.modules.stock.model.StockMovement.MovementReason;
import GestionPlat.example.demo.modules.stock.model.StockMovement.MovementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockAdjustmentRequest {
    private Long productId;
    private Integer quantity;
    private MovementType type;
    private MovementReason reason;
    private String note;
}
