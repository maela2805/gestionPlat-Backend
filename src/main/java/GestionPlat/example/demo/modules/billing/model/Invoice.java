package GestionPlat.example.demo.modules.billing.model;

import GestionPlat.example.demo.modules.boutique.model.Boutique;
import GestionPlat.example.demo.modules.purchase.model.PurchaseOrder;
import GestionPlat.example.demo.modules.sale.model.StoreSale;
import GestionPlat.example.demo.modules.tiers.model.Tiers;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_number", nullable = false, unique = true)
    private String invoiceNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private InvoiceStatus status = InvoiceStatus.BROUILLON;

    @Column(name = "invoice_date", nullable = false)
    private LocalDateTime invoiceDate;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tiers_id")
    private Tiers tiers;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "boutique_id")
    private Boutique boutique;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_sale_id")
    private StoreSale sourceSale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_purchase_order_id")
    private PurchaseOrder sourcePurchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_boutique_order_id")
    private GestionPlat.example.demo.modules.boutique.model.BoutiqueOrder sourceBoutiqueOrder;

    @Column(name = "subtotal_ht", nullable = false)
    @Builder.Default
    private BigDecimal subtotalHt = BigDecimal.ZERO;

    @Column(name = "tax_rate")
    @Builder.Default
    private BigDecimal taxRate = BigDecimal.ZERO;

    @Column(name = "tax_amount", nullable = false)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_ttc", nullable = false)
    @Builder.Default
    private BigDecimal totalTtc = BigDecimal.ZERO;

    @Column(name = "paid_amount", nullable = false)
    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "remaining_amount", nullable = false)
    @Builder.Default
    private BigDecimal remainingAmount = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String note;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<InvoiceItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
