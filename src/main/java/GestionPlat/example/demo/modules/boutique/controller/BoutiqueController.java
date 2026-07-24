package GestionPlat.example.demo.modules.boutique.controller;

import GestionPlat.example.demo.modules.boutique.dto.BoutiqueDTO;
import GestionPlat.example.demo.modules.boutique.dto.BoutiquePriceDTO;
import GestionPlat.example.demo.modules.boutique.dto.CreateBoutiqueRequest;
import GestionPlat.example.demo.modules.boutique.dto.SetWholesalePriceRequest;
import GestionPlat.example.demo.modules.boutique.service.BoutiqueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boutiques")
@RequiredArgsConstructor
@Tag(name = "Gestion des Boutiques & Prix de Cession", description = "Gestion des points de vente et grilles tarifaires de cession")
public class BoutiqueController {

    private final BoutiqueService boutiqueService;

    @Operation(summary = "Liste de toutes les boutiques")
    @GetMapping
    public ResponseEntity<List<BoutiqueDTO>> getAllBoutiques() {
        return ResponseEntity.ok(boutiqueService.getAllBoutiques());
    }

    @Operation(summary = "Obtenir les détails d'une boutique par ID")
    @GetMapping("/{id}")
    public ResponseEntity<BoutiqueDTO> getBoutiqueById(@PathVariable Long id) {
        return ResponseEntity.ok(boutiqueService.getBoutiqueById(id));
    }

    @Operation(summary = "Créer une nouvelle boutique")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_MANAGER', 'WRITE_STOCK')")
    public ResponseEntity<BoutiqueDTO> createBoutique(@Valid @RequestBody CreateBoutiqueRequest request) {
        return ResponseEntity.ok(boutiqueService.createBoutique(request));
    }

    @Operation(summary = "Modifier une boutique")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_MANAGER', 'WRITE_STOCK')")
    public ResponseEntity<BoutiqueDTO> updateBoutique(@PathVariable Long id, @Valid @RequestBody CreateBoutiqueRequest request) {
        return ResponseEntity.ok(boutiqueService.updateBoutique(id, request));
    }

    @Operation(summary = "Supprimer une boutique")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'WRITE_STOCK')")
    public ResponseEntity<Void> deleteBoutique(@PathVariable Long id) {
        boutiqueService.deleteBoutique(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Obtenir la grille de prix de cession d'une boutique")
    @GetMapping("/{id}/prices")
    public ResponseEntity<List<BoutiquePriceDTO>> getBoutiquePrices(@PathVariable Long id) {
        return ResponseEntity.ok(boutiqueService.getBoutiquePrices(id));
    }

    @Operation(summary = "Définir ou modifier le prix de cession d'un produit pour une boutique")
    @PostMapping("/{id}/prices")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_MANAGER', 'WRITE_STOCK')")
    public ResponseEntity<BoutiquePriceDTO> setWholesalePrice(
            @PathVariable Long id,
            @Valid @RequestBody SetWholesalePriceRequest request) {
        return ResponseEntity.ok(boutiqueService.setWholesalePrice(id, request));
    }
}
