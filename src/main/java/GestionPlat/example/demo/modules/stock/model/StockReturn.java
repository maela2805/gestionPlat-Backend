package GestionPlat.example.demo.modules.stock.model;

import GestionPlat.example.demo.modules.boutique.model.Boutique;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "stock_returns")
public class StockReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String reference;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "boutique_id", nullable = false)
    private Boutique boutique;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StockReturnType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StockReturnStatus status = StockReturnStatus.PENDING;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(name = "approved_by_email")
    private String approvedByEmail;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @ElementCollection
    @CollectionTable(name = "stock_return_media", joinColumns = @JoinColumn(name = "stock_return_id"))
    @Column(name = "media_url", length = 500)
    @Builder.Default
    private List<String> mediaUrls = new ArrayList<>();

    @OneToMany(mappedBy = "stockReturn", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<StockReturnItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
