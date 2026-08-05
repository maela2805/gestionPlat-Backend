package GestionPlat.example.demo.modules.stock.dto;

import GestionPlat.example.demo.modules.stock.model.StockReturnStatus;
import GestionPlat.example.demo.modules.stock.model.StockReturnType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockReturnDTO {
    private Long id;
    private String reference;
    private Long boutiqueId;
    private String boutiqueCode;
    private String boutiqueName;
    private StockReturnType type;
    private StockReturnStatus status;
    private String userEmail;
    private String approvedByEmail;
    private String description;
    private String rejectionReason;
    private List<String> mediaUrls;
    private List<StockReturnItemDTO> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
