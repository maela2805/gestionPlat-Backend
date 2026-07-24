package GestionPlat.example.demo.modules.sale.dto;

import GestionPlat.example.demo.modules.sale.model.StoreSaleStatus;
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
public class StoreSaleDTO {
    private Long id;
    private String reference;
    private Long boutiqueId;
    private String boutiqueCode;
    private String boutiqueName;
    private LocalDateTime saleDate;
    private StoreSaleStatus status;
    private BigDecimal totalAmount;
    private String userEmail;
    private String note;
    private List<StoreSaleItemDTO> items;
    private LocalDateTime createdAt;
}
