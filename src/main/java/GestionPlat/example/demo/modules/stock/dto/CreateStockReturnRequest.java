package GestionPlat.example.demo.modules.stock.dto;

import GestionPlat.example.demo.modules.stock.model.StockReturnType;
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
public class CreateStockReturnRequest {

    @NotNull(message = "La boutique est obligatoire")
    private Long boutiqueId;

    @NotNull(message = "Le type de retour est obligatoire")
    private StockReturnType type;

    private String description;

    private List<String> mediaUrls;

    @NotEmpty(message = "La déclaration doit contenir au moins un produit")
    private List<CreateStockReturnItemRequest> items;
}
