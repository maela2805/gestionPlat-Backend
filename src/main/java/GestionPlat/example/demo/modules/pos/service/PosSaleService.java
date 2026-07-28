package GestionPlat.example.demo.modules.pos.service;

import GestionPlat.example.demo.modules.pos.dto.CreatePosSaleRequest;
import GestionPlat.example.demo.modules.pos.dto.PosSaleDTO;

import java.util.List;

public interface PosSaleService {
    PosSaleDTO createPosSale(CreatePosSaleRequest request, String userEmail);
    PosSaleDTO getPosSaleById(Long id);
    PosSaleDTO getPosSaleByReceiptNumber(String receiptNumber);
    List<PosSaleDTO> getPosSalesBySession(Long cashSessionId);
    List<PosSaleDTO> getPosSalesByBoutique(Long boutiqueId);
    List<PosSaleDTO> getAllPosSales();
    PosSaleDTO cancelPosSale(Long id, String userEmail);
}
