package GestionPlat.example.demo.modules.stock.dto;

import lombok.Data;

@Data
public class InventoryItemRequest {
    private Long productId;
    private Integer countedQuantity;
}
