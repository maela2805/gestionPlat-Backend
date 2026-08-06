package GestionPlat.example.demo.modules.boutique.dto;

import GestionPlat.example.demo.modules.boutique.model.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateBoutiquePromotionRequest {

    @NotBlank(message = "Le nom de la promotion est obligatoire")
    private String name;

    private Long boutiqueId; // null for all boutiques, or specific boutiqueId

    @NotNull(message = "Le produit est obligatoire")
    private Long productId;

    @NotNull(message = "Le type de réduction est obligatoire")
    private DiscountType discountType;

    @NotNull(message = "La valeur de la réduction est obligatoire")
    private Double discountValue;

    @NotNull(message = "La date de début est obligatoire")
    private LocalDateTime startDate;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDateTime endDate;
}
