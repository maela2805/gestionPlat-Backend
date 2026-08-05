package GestionPlat.example.demo.modules.tiers.controller;

import GestionPlat.example.demo.modules.tiers.dto.CreateTiersRequest;
import GestionPlat.example.demo.modules.tiers.dto.TiersDTO;
import GestionPlat.example.demo.modules.tiers.dto.UpdateTiersRequest;
import GestionPlat.example.demo.modules.tiers.model.TiersStatus;
import GestionPlat.example.demo.modules.tiers.model.TiersType;
import GestionPlat.example.demo.modules.tiers.service.TiersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tiers")
@RequiredArgsConstructor
@Tag(name = "Gestion des Tiers", description = "Gestion des partenaires, fournisseurs et clients")
public class TiersController {

    private final TiersService tiersService;

    @Operation(summary = "Liste de tous les tiers (filtrable par type et statut)")
    @GetMapping
    public ResponseEntity<List<TiersDTO>> getAllTiers(
            @RequestParam(required = false) TiersType type,
            @RequestParam(required = false) TiersStatus status) {
        return ResponseEntity.ok(tiersService.getAllTiers(type, status));
    }

    @Operation(summary = "Obtenir les détails d'un tiers par ID")
    @GetMapping("/{id}")
    public ResponseEntity<TiersDTO> getTiersById(@PathVariable Long id) {
        return ResponseEntity.ok(tiersService.getTiersById(id));
    }

    @Operation(summary = "Créer un nouveau tiers")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_EMPLOYEE', 'EMPLOYEE', 'WRITE_STOCK')")
    public ResponseEntity<TiersDTO> createTiers(@Valid @RequestBody CreateTiersRequest request) {
        return ResponseEntity.ok(tiersService.createTiers(request));
    }

    @Operation(summary = "Mettre à jour un tiers")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_MANAGER', 'WRITE_STOCK')")
    public ResponseEntity<TiersDTO> updateTiers(@PathVariable Long id, @Valid @RequestBody UpdateTiersRequest request) {
        return ResponseEntity.ok(tiersService.updateTiers(id, request));
    }

    @Operation(summary = "Supprimer un tiers")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'WRITE_STOCK')")
    public ResponseEntity<Void> deleteTiers(@PathVariable Long id) {
        tiersService.deleteTiers(id);
        return ResponseEntity.noContent().build();
    }
}
