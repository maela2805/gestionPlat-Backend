package GestionPlat.example.demo.modules.boutique.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBoutiqueOrderRequest {
    private Long boutiqueId;
    private String note;
    private List<CreateBoutiqueOrderItemRequest> items;
}
