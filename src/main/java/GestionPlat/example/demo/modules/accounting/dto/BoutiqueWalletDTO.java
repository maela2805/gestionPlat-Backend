package GestionPlat.example.demo.modules.accounting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoutiqueWalletDTO {
    private Long boutiqueId;
    private String boutiqueName;
    private BigDecimal totalCessionInvoicesAmount; // Somme factures de cession d'approvisionnement dues au dépôt
    private BigDecimal totalPaidAmount;            // Somme versements validés (APPROVED)
    private BigDecimal balanceDue;                 // Solde restant dû au dépôt = Factures - Payé
    private BigDecimal pendingTransfersAmount;     // Somme versements en attente de validation (PENDING)
}
