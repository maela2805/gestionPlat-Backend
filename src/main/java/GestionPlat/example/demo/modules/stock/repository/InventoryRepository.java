package GestionPlat.example.demo.modules.stock.repository;

import GestionPlat.example.demo.modules.stock.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    List<Inventory> findAllByOrderByCreatedAtDesc();
    List<Inventory> findByBoutiqueIdOrderByCreatedAtDesc(Long boutiqueId);
}
