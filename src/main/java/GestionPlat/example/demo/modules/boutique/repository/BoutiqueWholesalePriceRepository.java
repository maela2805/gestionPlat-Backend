package GestionPlat.example.demo.modules.boutique.repository;

import GestionPlat.example.demo.modules.boutique.model.BoutiqueWholesalePrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoutiqueWholesalePriceRepository extends JpaRepository<BoutiqueWholesalePrice, Long> {
    List<BoutiqueWholesalePrice> findByBoutiqueId(Long boutiqueId);
    Optional<BoutiqueWholesalePrice> findByBoutiqueIdAndProductId(Long boutiqueId, Long productId);
    List<BoutiqueWholesalePrice> findByProductId(Long productId);
}
