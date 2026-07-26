package GestionPlat.example.demo.modules.stock.service;

import GestionPlat.example.demo.modules.stock.dto.StockAdjustmentRequest;
import GestionPlat.example.demo.modules.stock.model.Product;
import GestionPlat.example.demo.modules.stock.model.StockMovement;

import java.util.List;

public interface StockMovementService {
    Product adjustStock(StockAdjustmentRequest request, String userEmail);
    List<StockMovement> getAllStockMovements();
    List<StockMovement> getMovementsByProduct(Long productId);
    List<StockMovement> getMovementsByBoutique(Long boutiqueId);
}
