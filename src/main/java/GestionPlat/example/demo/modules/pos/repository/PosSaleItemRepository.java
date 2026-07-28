package GestionPlat.example.demo.modules.pos.repository;

import GestionPlat.example.demo.modules.pos.model.PosSaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PosSaleItemRepository extends JpaRepository<PosSaleItem, Long> {
    List<PosSaleItem> findByPosSaleId(Long posSaleId);
}
