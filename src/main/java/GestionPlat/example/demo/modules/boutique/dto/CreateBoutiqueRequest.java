package GestionPlat.example.demo.modules.boutique.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBoutiqueRequest {
    private String code;

    @NotBlank(message = "Le nom de la boutique est obligatoire")
    private String name;

    private String address;
    private String city;
    private String phone;
    private String managerName;
    private Boolean active;
}
