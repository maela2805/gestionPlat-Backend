package GestionPlat.example.demo.modules.caisse.dto;

import GestionPlat.example.demo.modules.caisse.model.CashMovementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashMovementDTO {
    private Long id;
    private Long cashSessionId;
    private CashMovementType type;
    private BigDecimal amount;
    private String reason;
    private String userEmail;
    private LocalDateTime createdAt;
}
