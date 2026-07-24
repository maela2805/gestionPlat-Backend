package GestionPlat.example.demo.modules.sale.controller;

import GestionPlat.example.demo.modules.sale.dto.CreateStoreSaleRequest;
import GestionPlat.example.demo.modules.sale.dto.StoreSaleDTO;
import GestionPlat.example.demo.modules.sale.service.StoreSaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/store-sales")
@RequiredArgsConstructor
@Tag(name = "Ventes & Expéditions aux Boutiques", description = "Gestion des ventes gros/cession d'expéditions aux boutiques")
public class StoreSaleController {

    private final StoreSaleService storeSaleService;

    @Operation(summary = "Liste de toutes les ventes/expéditions aux boutiques")
    @GetMapping
    public ResponseEntity<List<StoreSaleDTO>> getAllStoreSales() {
        return ResponseEntity.ok(storeSaleService.getAllStoreSales());
    }

    @Operation(summary = "Obtenir les détails d'une vente boutique")
    @GetMapping("/{id}")
    public ResponseEntity<StoreSaleDTO> getStoreSaleById(@PathVariable Long id) {
        return ResponseEntity.ok(storeSaleService.getStoreSaleById(id));
    }

    @Operation(summary = "Créer un nouveau bon de vente/expédition à une boutique")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_MANAGER', 'WRITE_STOCK')")
    public ResponseEntity<StoreSaleDTO> createStoreSale(@Valid @RequestBody CreateStoreSaleRequest request, Authentication authentication) {
        String userEmail = authentication != null ? authentication.getName() : "SYSTEM";
        return ResponseEntity.ok(storeSaleService.createStoreSale(request, userEmail));
    }

    @Operation(summary = "Valider l'expédition d'une vente (Déduit le stock central et génère une sortie de stock)")
    @PutMapping("/{id}/validate")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_MANAGER', 'WRITE_STOCK')")
    public ResponseEntity<StoreSaleDTO> validateStoreSale(@PathVariable Long id, Authentication authentication) {
        String userEmail = authentication != null ? authentication.getName() : "SYSTEM";
        return ResponseEntity.ok(storeSaleService.validateStoreSale(id, userEmail));
    }

    @Operation(summary = "Annuler une vente boutique")
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_MANAGER', 'WRITE_STOCK')")
    public ResponseEntity<StoreSaleDTO> cancelStoreSale(@PathVariable Long id) {
        return ResponseEntity.ok(storeSaleService.cancelStoreSale(id));
    }
}
