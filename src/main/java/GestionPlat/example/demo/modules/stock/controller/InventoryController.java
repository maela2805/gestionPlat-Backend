package GestionPlat.example.demo.modules.stock.controller;

import GestionPlat.example.demo.modules.stock.dto.CreateInventoryRequest;
import GestionPlat.example.demo.modules.stock.model.Inventory;
import GestionPlat.example.demo.modules.stock.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventories")
@RequiredArgsConstructor
@Tag(name = "4. Gestion de Stock - Inventaire", description = "Comptage périodique et ajustements de stock par entrepôt")
public class InventoryController {

    private final InventoryService inventoryService;

    @Operation(summary = "Lister tous les inventaires (filtrable par entrepôt)")
    @GetMapping
    public ResponseEntity<List<Inventory>> getAllInventories(@RequestParam(required = false) Long boutiqueId) {
        return ResponseEntity.ok(inventoryService.getAllInventories(boutiqueId));
    }

    @Operation(summary = "Détail d'un inventaire")
    @GetMapping("/{id}")
    public ResponseEntity<Inventory> getInventoryById(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getInventoryById(id));
    }

    @Operation(summary = "Créer un nouvel inventaire périodique")
    @PostMapping
    public ResponseEntity<Inventory> createInventory(@RequestBody CreateInventoryRequest request, Authentication authentication) {
        String email = authentication != null ? authentication.getName() : "admin@system.com";
        return ResponseEntity.ok(inventoryService.createInventory(request, email));
    }

    @Operation(summary = "Valider un inventaire (ajuste les stocks)")
    @PutMapping("/{id}/validate")
    public ResponseEntity<Inventory> validateInventory(@PathVariable Long id, Authentication authentication) {
        String email = authentication != null ? authentication.getName() : "admin@system.com";
        return ResponseEntity.ok(inventoryService.validateInventory(id, email));
    }
}
