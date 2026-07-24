package GestionPlat.example.demo.modules.tiers.dto;

import GestionPlat.example.demo.modules.tiers.model.TiersStatus;
import GestionPlat.example.demo.modules.tiers.model.TiersType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TiersDTO {
    private Long id;
    private String code;
    private String name;
    private TiersType type;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String taxId;
    private TiersStatus status;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
