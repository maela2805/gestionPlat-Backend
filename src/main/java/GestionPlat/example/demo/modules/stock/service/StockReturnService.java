package GestionPlat.example.demo.modules.stock.service;

import GestionPlat.example.demo.modules.stock.dto.CreateStockReturnRequest;
import GestionPlat.example.demo.modules.stock.dto.StockReturnDTO;
import GestionPlat.example.demo.modules.stock.model.StockReturnStatus;

import java.util.List;

public interface StockReturnService {
    List<StockReturnDTO> getAllStockReturns(Long boutiqueId, StockReturnStatus status);
    StockReturnDTO getStockReturnById(Long id);
    StockReturnDTO createStockReturn(CreateStockReturnRequest request, String userEmail);
    StockReturnDTO approveStockReturn(Long id, String userEmail);
    StockReturnDTO rejectStockReturn(Long id, String reason, String userEmail);
    StockReturnDTO cancelStockReturn(Long id, String userEmail);
}
