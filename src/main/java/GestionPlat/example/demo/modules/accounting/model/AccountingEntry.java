package GestionPlat.example.demo.modules.accounting.model;

import GestionPlat.example.demo.modules.billing.model.Invoice;
import GestionPlat.example.demo.modules.billing.model.PaymentMethod;
import GestionPlat.example.demo.modules.caisse.model.CashSession;
import GestionPlat.example.demo.modules.tiers.model.Tiers;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "accounting_entries")
public class AccountingEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entry_code", nullable = false, unique = true)
    private String entryCode;

    @Column(name = "entry_date", nullable = false)
    private LocalDateTime entryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntryType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountingCategory category;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Column(nullable = false)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_invoice_id")
    private Invoice sourceInvoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_cash_session_id")
    private CashSession sourceCashSession;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tiers_id")
    private Tiers tiers;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
