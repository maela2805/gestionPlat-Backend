package GestionPlat.example.demo.modules.boutique.service;

import GestionPlat.example.demo.modules.billing.dto.InvoiceDTO;
import GestionPlat.example.demo.modules.boutique.dto.ApproveBoutiqueOrderRequest;
import GestionPlat.example.demo.modules.boutique.dto.BoutiqueOrderDTO;
import GestionPlat.example.demo.modules.boutique.dto.CreateBoutiqueOrderRequest;

import java.util.List;

public interface BoutiqueOrderService {
    List<BoutiqueOrderDTO> getAllOrders();
    List<BoutiqueOrderDTO> getOrdersByBoutique(Long boutiqueId);
    BoutiqueOrderDTO getOrderById(Long id);
    BoutiqueOrderDTO createOrder(CreateBoutiqueOrderRequest request, String userEmail);
    BoutiqueOrderDTO approveOrder(Long id, ApproveBoutiqueOrderRequest request, String userEmail);
    BoutiqueOrderDTO rejectOrder(Long id, String reason);
    BoutiqueOrderDTO cancelOrder(Long id);
    void deleteOrder(Long id);
    InvoiceDTO createInvoiceForOrder(Long orderId);
}
