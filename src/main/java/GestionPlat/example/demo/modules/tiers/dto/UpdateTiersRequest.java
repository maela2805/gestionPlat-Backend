package GestionPlat.example.demo.modules.tiers.dto;

import GestionPlat.example.demo.modules.tiers.model.TiersStatus;
import GestionPlat.example.demo.modules.tiers.model.TiersType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTiersRequest {
    @NotBlank(message = "Le nom du tiers est obligatoire")
    private String name;

    @NotNull(message = "Le type de tiers est obligatoire")
    private TiersType type;

    private String email;
    private String phone;
    private String address;
    private String city;
    private String taxId;

    @NotNull(message = "Le statut est obligatoire")
    private TiersStatus status;

    private String note;
}
