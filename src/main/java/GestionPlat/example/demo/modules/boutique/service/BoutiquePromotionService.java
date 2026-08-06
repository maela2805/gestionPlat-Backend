package GestionPlat.example.demo.modules.boutique.service;

import GestionPlat.example.demo.modules.boutique.dto.BoutiquePromotionDTO;
import GestionPlat.example.demo.modules.boutique.dto.CreateBoutiquePromotionRequest;

import java.util.List;

public interface BoutiquePromotionService {
    List<BoutiquePromotionDTO> getAllPromotions();
    List<BoutiquePromotionDTO> getPromotionsByBoutique(Long boutiqueId);
    List<BoutiquePromotionDTO> getActivePromotionsForBoutique(Long boutiqueId);
    BoutiquePromotionDTO getActivePromotionForProduct(Long boutiqueId, Long productId);
    BoutiquePromotionDTO createPromotion(CreateBoutiquePromotionRequest request);
    void deletePromotion(Long id);
    void togglePromotionStatus(Long id);
}
