package GestionPlat.example.demo.modules.purchase.service;

import GestionPlat.example.demo.modules.purchase.dto.CreatePurchaseOrderRequest;
import GestionPlat.example.demo.modules.purchase.dto.PurchaseOrderDTO;

import java.util.List;

public interface PurchaseOrderService {
    List<PurchaseOrderDTO> getAllPurchaseOrders();
    PurchaseOrderDTO getPurchaseOrderById(Long id);
    PurchaseOrderDTO createPurchaseOrder(CreatePurchaseOrderRequest request);
    PurchaseOrderDTO validatePurchaseOrder(Long id);
    PurchaseOrderDTO receiveDelivery(Long id, String userEmail);
    PurchaseOrderDTO cancelPurchaseOrder(Long id);
}
