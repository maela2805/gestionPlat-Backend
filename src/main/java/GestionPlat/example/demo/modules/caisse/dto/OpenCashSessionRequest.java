package GestionPlat.example.demo.modules.caisse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpenCashSessionRequest {
    private Long boutiqueId;
    private BigDecimal openingBalance;
    private String notes;
}
