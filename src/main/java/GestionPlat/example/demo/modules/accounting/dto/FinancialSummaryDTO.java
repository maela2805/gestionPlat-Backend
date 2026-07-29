package GestionPlat.example.demo.modules.accounting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialSummaryDTO {
    private BigDecimal totalRevenue; // Total Recettes / Chiffre d'Affaires
    private BigDecimal totalExpense; // Total Dépenses / Charges
    private BigDecimal netProfit;    // Résultat Net (Bénéfice / Perte)
    private BigDecimal clientReceivables; // Créances clients (Factures ventes non payées)
    private BigDecimal supplierPayables;   // Dettes fournisseurs (Factures achats non payées)
    private BigDecimal cashBalance;        // Solde Trésorerie
    private Map<String, BigDecimal> expensesByCategory; // Ventilation des dépenses
}
