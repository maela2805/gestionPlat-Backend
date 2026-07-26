package GestionPlat.example.demo.modules.stock.service;

import GestionPlat.example.demo.modules.stock.dto.BoutiqueStockDTO;
import GestionPlat.example.demo.modules.stock.dto.TransferStockRequest;

import java.util.List;

public interface BoutiqueStockService {
    List<BoutiqueStockDTO> getStocksByBoutique(Long boutiqueId);
    List<BoutiqueStockDTO> getAllBoutiqueStocks();
    void transferStock(TransferStockRequest request, String userEmail);
}
