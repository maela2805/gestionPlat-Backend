package GestionPlat.example.demo.modules.boutique.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoutiqueDTO {
    private Long id;
    private String code;
    private String name;
    private String address;
    private String city;
    private String phone;
    private String managerName;
    private Boolean active;
    private LocalDateTime createdAt;
}
