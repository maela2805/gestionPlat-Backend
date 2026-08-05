package GestionPlat.example.demo.modules.billing.dto;

import GestionPlat.example.demo.modules.billing.model.InvoiceStatus;
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
public class UpdateInvoiceRequest {
    private LocalDateTime invoiceDate;
    private LocalDateTime dueDate;
    private Long tiersId;
    private Long boutiqueId;
    private BigDecimal taxRate;
    private String note;
    private BigDecimal paidAmount;
    private InvoiceStatus status;
    private LocalDateTime deliveryDate;
    private String driverName;
    private String driverPhone;
    private String vehicleRegistration;
    private String attachmentUrl;
    private List<CreateInvoiceItemRequest> items;
}
