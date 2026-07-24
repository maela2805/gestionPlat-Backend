package GestionPlat.example.demo.modules.purchase.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePurchaseOrderRequest {
    private String reference;

    @NotNull(message = "Le fournisseur est obligatoire")
    private Long supplierId;

    private LocalDateTime expectedDeliveryDate;
    private String note;

    @NotEmpty(message = "La commande doit contenir au moins un produit")
    private List<CreatePurchaseOrderItemRequest> items;
}
