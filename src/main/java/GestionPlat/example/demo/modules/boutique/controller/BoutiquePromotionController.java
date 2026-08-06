package GestionPlat.example.demo.modules.boutique.controller;

import GestionPlat.example.demo.modules.boutique.dto.BoutiquePromotionDTO;
import GestionPlat.example.demo.modules.boutique.dto.CreateBoutiquePromotionRequest;
import GestionPlat.example.demo.modules.boutique.service.BoutiquePromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
@Tag(name = "Gestion des Promotions par Boutique", description = "Gestion des promotions et réductions temporelles par boutique")
public class BoutiquePromotionController {

    private final BoutiquePromotionService promotionService;

    @Operation(summary = "Obtenir toutes les promotions")
    @GetMapping
    public ResponseEntity<List<BoutiquePromotionDTO>> getAllPromotions(
            @RequestParam(required = false) Long boutiqueId,
            org.springframework.security.core.Authentication authentication) {

        if (authentication != null && authentication.getPrincipal() instanceof GestionPlat.example.demo.modules.auth.model.User) {
            GestionPlat.example.demo.modules.auth.model.User user = (GestionPlat.example.demo.modules.auth.model.User) authentication.getPrincipal();
            boolean isAdmin = user.getRole() != null && (
                    "ROLE_SUPER_ADMIN".equalsIgnoreCase(user.getRole().getName()) ||
                    "ROLE_ADMIN".equalsIgnoreCase(user.getRole().getName())
            );
            if (!isAdmin && user.getBoutique() != null) {
                boutiqueId = user.getBoutique().getId();
            }
        }

        if (boutiqueId != null) {
            return ResponseEntity.ok(promotionService.getPromotionsByBoutique(boutiqueId));
        }
        return ResponseEntity.ok(promotionService.getAllPromotions());
    }

    @Operation(summary = "Obtenir les promotions actives pour une boutique")
    @GetMapping("/active")
    public ResponseEntity<List<BoutiquePromotionDTO>> getActivePromotions(@RequestParam Long boutiqueId) {
        return ResponseEntity.ok(promotionService.getActivePromotionsForBoutique(boutiqueId));
    }

    @Operation(summary = "Obtenir la promotion active pour un produit dans une boutique")
    @GetMapping("/active-product")
    public ResponseEntity<BoutiquePromotionDTO> getActivePromotionForProduct(
            @RequestParam Long boutiqueId,
            @RequestParam Long productId) {
        return ResponseEntity.ok(promotionService.getActivePromotionForProduct(boutiqueId, productId));
    }

    @Operation(summary = "Créer une nouvelle promotion pour une boutique (Administrateurs)")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<BoutiquePromotionDTO> createPromotion(@Valid @RequestBody CreateBoutiquePromotionRequest request) {
        return ResponseEntity.ok(promotionService.createPromotion(request));
    }

    @Operation(summary = "Activer ou désactiver une promotion (Administrateurs)")
    @PutMapping("/{id}/toggle-status")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<Void> toggleStatus(@PathVariable Long id) {
        promotionService.togglePromotionStatus(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Supprimer une promotion (Administrateurs)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<Void> deletePromotion(@PathVariable Long id) {
        promotionService.deletePromotion(id);
        return ResponseEntity.noContent().build();
    }
}
