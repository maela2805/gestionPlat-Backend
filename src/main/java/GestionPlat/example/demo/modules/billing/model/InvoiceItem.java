package GestionPlat.example.demo.modules.billing.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "invoice_items")
public class InvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    @ToString.Exclude
    @JsonIgnore
    private Invoice invoice;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal quantity = BigDecimal.ONE;

    @Column(name = "unit_price_ht", nullable = false)
    @Builder.Default
    private BigDecimal unitPriceHt = BigDecimal.ZERO;

    @Column(name = "tax_rate")
    @Builder.Default
    private BigDecimal taxRate = BigDecimal.ZERO;

    @Column(name = "total_ht", nullable = false)
    @Builder.Default
    private BigDecimal totalHt = BigDecimal.ZERO;

    @Column(name = "total_ttc", nullable = false)
    @Builder.Default
    private BigDecimal totalTtc = BigDecimal.ZERO;
}
