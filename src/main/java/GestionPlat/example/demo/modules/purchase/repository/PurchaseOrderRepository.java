package GestionPlat.example.demo.modules.purchase.repository;

import GestionPlat.example.demo.modules.purchase.model.PurchaseOrder;
import GestionPlat.example.demo.modules.purchase.model.PurchaseOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    boolean existsByReference(String reference);
    List<PurchaseOrder> findBySupplierId(Long supplierId);
    List<PurchaseOrder> findByStatus(PurchaseOrderStatus status);
}
