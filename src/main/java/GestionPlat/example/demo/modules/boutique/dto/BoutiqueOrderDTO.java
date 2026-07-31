package GestionPlat.example.demo.modules.boutique.dto;

import GestionPlat.example.demo.modules.boutique.model.BoutiqueOrderStatus;
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
public class BoutiqueOrderDTO {
    private Long id;
    private String orderNumber;
    private Long boutiqueId;
    private String boutiqueName;
    private LocalDateTime orderDate;
    private BoutiqueOrderStatus status;
    private BigDecimal totalAmount;
    private LocalDateTime deliveryDate;
    private String vehicleRegistration;
    private String driverName;
    private String driverPhone;
    private String attachmentUrl;
    private String boutiqueSignature;
    private String depotSignature;
    private Boolean invoiceCreated;
    private Long invoiceId;
    private String invoiceNumber;
    private String note;
    private String createdByUser;
    private List<BoutiqueOrderItemDTO> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
