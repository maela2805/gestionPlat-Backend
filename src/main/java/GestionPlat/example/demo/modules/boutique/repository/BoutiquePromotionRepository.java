package GestionPlat.example.demo.modules.boutique.repository;

import GestionPlat.example.demo.modules.boutique.model.BoutiquePromotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BoutiquePromotionRepository extends JpaRepository<BoutiquePromotion, Long> {

    List<BoutiquePromotion> findByBoutiqueId(Long boutiqueId);

    @Query("SELECT p FROM BoutiquePromotion p WHERE (p.boutique.id = :boutiqueId OR p.boutique IS NULL) " +
           "AND p.active = true AND p.startDate <= :now AND p.endDate >= :now")
    List<BoutiquePromotion> findActivePromotionsForBoutique(@Param("boutiqueId") Long boutiqueId, @Param("now") LocalDateTime now);

    @Query("SELECT p FROM BoutiquePromotion p WHERE (p.boutique.id = :boutiqueId OR p.boutique IS NULL) " +
           "AND p.product.id = :productId AND p.active = true AND p.startDate <= :now AND p.endDate >= :now " +
           "ORDER BY p.boutique.id DESC NULLS LAST")
    List<BoutiquePromotion> findActivePromotionForProductInBoutique(@Param("boutiqueId") Long boutiqueId, @Param("productId") Long productId, @Param("now") LocalDateTime now);
}
