package GestionPlat.example.demo.modules.caisse.model;

import GestionPlat.example.demo.modules.boutique.model.Boutique;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "cash_sessions")
public class CashSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_reference", nullable = false, unique = true)
    private String sessionReference;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "boutique_id", nullable = false)
    private Boutique boutique;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(name = "opening_date", nullable = false)
    private LocalDateTime openingDate;

    @Column(name = "closing_date")
    private LocalDateTime closingDate;

    @Column(name = "opening_balance", nullable = false)
    @Builder.Default
    private BigDecimal openingBalance = BigDecimal.ZERO;

    @Column(name = "closing_balance_expected")
    @Builder.Default
    private BigDecimal closingBalanceExpected = BigDecimal.ZERO;

    @Column(name = "closing_balance_real")
    private BigDecimal closingBalanceReal;

    @Column(name = "cash_difference")
    @Builder.Default
    private BigDecimal cashDifference = BigDecimal.ZERO;

    @Column(name = "total_sales_cash")
    @Builder.Default
    private BigDecimal totalSalesCash = BigDecimal.ZERO;

    @Column(name = "total_sales_mobile_money")
    @Builder.Default
    private BigDecimal totalSalesMobileMoney = BigDecimal.ZERO;

    @Column(name = "total_sales_card")
    @Builder.Default
    private BigDecimal totalSalesCard = BigDecimal.ZERO;

    @Column(name = "total_sales_other")
    @Builder.Default
    private BigDecimal totalSalesOther = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CashSessionStatus status = CashSessionStatus.OPEN;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
