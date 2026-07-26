package GestionPlat.example.demo.modules.stock.service;

import GestionPlat.example.demo.modules.stock.dto.CreateInventoryRequest;
import GestionPlat.example.demo.modules.stock.model.Inventory;

import java.util.List;

public interface InventoryService {
    Inventory createInventory(CreateInventoryRequest request, String userEmail);
    Inventory validateInventory(Long inventoryId, String userEmail);
    Inventory getInventoryById(Long inventoryId);
    List<Inventory> getAllInventories(Long boutiqueId);
}
