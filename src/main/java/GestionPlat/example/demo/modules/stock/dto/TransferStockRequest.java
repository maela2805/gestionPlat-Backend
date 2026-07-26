package GestionPlat.example.demo.modules.stock.dto;

import lombok.Data;

@Data
public class TransferStockRequest {
    private Long fromBoutiqueId; // null = Entrepôt Central
    private Long toBoutiqueId;   // null = Entrepôt Central
    private Long productId;
    private Integer quantity;
    private String note;
}
