package GestionPlat.example.demo.modules.caisse.dto;

import GestionPlat.example.demo.modules.caisse.model.CashSessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashSessionDTO {
    private Long id;
    private String sessionReference;
    private Long boutiqueId;
    private String boutiqueCode;
    private String boutiqueName;
    private String userEmail;
    private LocalDateTime openingDate;
    private LocalDateTime closingDate;
    private BigDecimal openingBalance;
    private BigDecimal closingBalanceExpected;
    private BigDecimal closingBalanceReal;
    private BigDecimal cashDifference;
    private BigDecimal totalSalesCash;
    private BigDecimal totalSalesMobileMoney;
    private BigDecimal totalSalesCard;
    private BigDecimal totalSalesOther;
    private BigDecimal totalCashIn;
    private BigDecimal totalCashOut;
    private CashSessionStatus status;
    private String notes;
    private List<CashMovementDTO> movements;
    private LocalDateTime createdAt;
}
