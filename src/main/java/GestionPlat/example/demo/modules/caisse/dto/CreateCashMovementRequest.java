package GestionPlat.example.demo.modules.caisse.dto;

import GestionPlat.example.demo.modules.caisse.model.CashMovementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCashMovementRequest {
    private CashMovementType type;
    private BigDecimal amount;
    private String reason;
}
