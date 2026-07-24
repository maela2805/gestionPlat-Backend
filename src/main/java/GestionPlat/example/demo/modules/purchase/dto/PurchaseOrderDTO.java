package GestionPlat.example.demo.modules.purchase.dto;

import GestionPlat.example.demo.modules.purchase.model.PurchaseOrderStatus;
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
public class PurchaseOrderDTO {
    private Long id;
    private String reference;
    private Long supplierId;
    private String supplierCode;
    private String supplierName;
    private LocalDateTime orderDate;
    private LocalDateTime expectedDeliveryDate;
    private LocalDateTime deliveryDate;
    private PurchaseOrderStatus status;
    private BigDecimal totalAmount;
    private String note;
    private List<PurchaseOrderItemDTO> items;
    private LocalDateTime createdAt;
}
