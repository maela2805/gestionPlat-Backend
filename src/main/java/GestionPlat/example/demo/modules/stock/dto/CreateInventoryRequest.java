package GestionPlat.example.demo.modules.stock.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateInventoryRequest {
    private Long boutiqueId;
    private String note;
    private List<InventoryItemRequest> items;
}
