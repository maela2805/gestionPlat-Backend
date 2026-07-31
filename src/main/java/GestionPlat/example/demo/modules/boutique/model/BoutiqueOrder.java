package GestionPlat.example.demo.modules.boutique.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
@Table(name = "boutique_orders")
public class BoutiqueOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "boutique_id", nullable = false)
    private Boutique boutique;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BoutiqueOrderStatus status = BoutiqueOrderStatus.PENDING;

    @Column(name = "total_amount", nullable = false)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "delivery_date")
    private LocalDateTime deliveryDate;

    @Column(name = "vehicle_registration")
    private String vehicleRegistration;

    @Column(name = "driver_name")
    private String driverName;

    @Column(name = "driver_phone")
    private String driverPhone;

    @Column(name = "attachment_url", columnDefinition = "TEXT")
    private String attachmentUrl;

    @Column(name = "boutique_signature", columnDefinition = "TEXT")
    private String boutiqueSignature;

    @Column(name = "depot_signature", columnDefinition = "TEXT")
    private String depotSignature;

    @Column(name = "invoice_created")
    @Builder.Default
    private Boolean invoiceCreated = false;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "created_by_user")
    private String createdByUser;

    @OneToMany(mappedBy = "boutiqueOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BoutiqueOrderItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
