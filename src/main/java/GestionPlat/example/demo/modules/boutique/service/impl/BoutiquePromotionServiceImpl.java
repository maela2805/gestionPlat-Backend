package GestionPlat.example.demo.modules.boutique.service.impl;

import GestionPlat.example.demo.modules.boutique.dto.BoutiquePromotionDTO;
import GestionPlat.example.demo.modules.boutique.dto.CreateBoutiquePromotionRequest;
import GestionPlat.example.demo.modules.boutique.model.Boutique;
import GestionPlat.example.demo.modules.boutique.model.BoutiquePromotion;
import GestionPlat.example.demo.modules.boutique.model.DiscountType;
import GestionPlat.example.demo.modules.boutique.repository.BoutiquePromotionRepository;
import GestionPlat.example.demo.modules.boutique.repository.BoutiqueRepository;
import GestionPlat.example.demo.modules.boutique.service.BoutiquePromotionService;
import GestionPlat.example.demo.modules.stock.model.Product;
import GestionPlat.example.demo.modules.stock.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoutiquePromotionServiceImpl implements BoutiquePromotionService {

    private final BoutiquePromotionRepository promotionRepository;
    private final BoutiqueRepository boutiqueRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public List<BoutiquePromotionDTO> getAllPromotions() {
        return promotionRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoutiquePromotionDTO> getPromotionsByBoutique(Long boutiqueId) {
        if (boutiqueId == null) {
            return getAllPromotions();
        }
        return promotionRepository.findAll().stream()
                .filter(p -> p.getBoutique() == null || (p.getBoutique() != null && p.getBoutique().getId().equals(boutiqueId)))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoutiquePromotionDTO> getActivePromotionsForBoutique(Long boutiqueId) {
        return promotionRepository.findActivePromotionsForBoutique(boutiqueId, LocalDateTime.now()).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BoutiquePromotionDTO getActivePromotionForProduct(Long boutiqueId, Long productId) {
        List<BoutiquePromotion> promos = promotionRepository.findActivePromotionForProductInBoutique(boutiqueId, productId, LocalDateTime.now());
        if (promos.isEmpty()) {
            return null;
        }
        return mapToDTO(promos.get(0));
    }

    @Override
    @Transactional
    public BoutiquePromotionDTO createPromotion(CreateBoutiquePromotionRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'ID: " + request.getProductId()));

        Boutique boutique = null;
        if (request.getBoutiqueId() != null) {
            boutique = boutiqueRepository.findById(request.getBoutiqueId())
                    .orElseThrow(() -> new RuntimeException("Boutique non trouvée avec l'ID: " + request.getBoutiqueId()));
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("La date de fin doit être supérieure à la date de début.");
        }

        BoutiquePromotion promo = BoutiquePromotion.builder()
                .name(request.getName())
                .boutique(boutique)
                .product(product)
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .active(true)
                .build();

        return mapToDTO(promotionRepository.save(promo));
    }

    @Override
    @Transactional
    public void deletePromotion(Long id) {
        if (!promotionRepository.existsById(id)) {
            throw new RuntimeException("Promotion introuvable avec l'ID: " + id);
        }
        promotionRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void togglePromotionStatus(Long id) {
        BoutiquePromotion promo = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion introuvable avec l'ID: " + id));
        promo.setActive(!promo.isActive());
        promotionRepository.save(promo);
    }

    private BoutiquePromotionDTO mapToDTO(BoutiquePromotion promo) {
        Product p = promo.getProduct();
        Double originalPrice = (p != null && p.getSellPrice() != null) ? p.getSellPrice().doubleValue() : 0.0;
        Double promoPrice = originalPrice;

        if (originalPrice != null && originalPrice > 0 && promo.getDiscountValue() != null) {
            if (promo.getDiscountType() == DiscountType.PERCENTAGE) {
                promoPrice = originalPrice * (1.0 - (promo.getDiscountValue() / 100.0));
            } else if (promo.getDiscountType() == DiscountType.FIXED_AMOUNT) {
                promoPrice = Math.max(0.0, originalPrice - promo.getDiscountValue());
            }
        }

        LocalDateTime now = LocalDateTime.now();
        boolean isCurrentlyActive = promo.isActive() && 
                !promo.getStartDate().isAfter(now) && 
                !promo.getEndDate().isBefore(now);

        return BoutiquePromotionDTO.builder()
                .id(promo.getId())
                .name(promo.getName())
                .boutiqueId(promo.getBoutique() != null ? promo.getBoutique().getId() : null)
                .boutiqueName(promo.getBoutique() != null ? promo.getBoutique().getName() : "Toutes les boutiques")
                .productId(p != null ? p.getId() : null)
                .productName(p != null ? p.getName() : null)
                .productReference(p != null ? p.getReference() : null)
                .originalPrice(originalPrice)
                .promoPrice(Math.round(promoPrice * 100.0) / 100.0)
                .discountType(promo.getDiscountType())
                .discountValue(promo.getDiscountValue())
                .startDate(promo.getStartDate())
                .endDate(promo.getEndDate())
                .active(promo.isActive())
                .currentlyActive(isCurrentlyActive)
                .build();
    }
}
