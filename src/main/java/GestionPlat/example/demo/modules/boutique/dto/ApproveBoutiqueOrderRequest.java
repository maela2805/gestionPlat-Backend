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
public class ApproveBoutiqueOrderRequest {
    private String deliveryDate; // YYYY-MM-DD ou ISO
    private String vehicleRegistration;
    private String driverName;
    private String driverPhone;
    private String attachmentUrl;
    private List<CreateBoutiqueOrderItemRequest> modifiedItems;
}
