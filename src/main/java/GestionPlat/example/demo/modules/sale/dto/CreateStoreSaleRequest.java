package GestionPlat.example.demo.modules.sale.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateStoreSaleRequest {
    private String reference;

    @NotNull(message = "La boutique est obligatoire")
    private Long boutiqueId;

    private String note;

    @NotEmpty(message = "La vente doit contenir au moins un produit")
    private List<CreateStoreSaleItemRequest> items;
}
