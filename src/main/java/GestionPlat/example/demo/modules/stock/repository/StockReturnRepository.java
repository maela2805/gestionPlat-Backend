package GestionPlat.example.demo.modules.stock.repository;

import GestionPlat.example.demo.modules.stock.model.StockReturn;
import GestionPlat.example.demo.modules.stock.model.StockReturnStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockReturnRepository extends JpaRepository<StockReturn, Long> {
    List<StockReturn> findByBoutiqueId(Long boutiqueId);
    List<StockReturn> findByStatus(StockReturnStatus status);
    List<StockReturn> findByBoutiqueIdAndStatus(Long boutiqueId, StockReturnStatus status);
}
