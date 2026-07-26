package GestionPlat.example.demo.modules.stock.controller;

import GestionPlat.example.demo.modules.stock.dto.BoutiqueStockDTO;
import GestionPlat.example.demo.modules.stock.dto.TransferStockRequest;
import GestionPlat.example.demo.modules.stock.service.BoutiqueStockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
@Tag(name = "4. Gestion de Stock - Entrepôts par Boutique", description = "Consultation du stock par entrepôt et transferts de stock entre boutiques")
public class WarehouseStockController {

    private final BoutiqueStockService boutiqueStockService;

    @Operation(summary = "Consulter le stock d'un entrepôt / boutique")
    @GetMapping("/boutique/{boutiqueId}")
    public ResponseEntity<List<BoutiqueStockDTO>> getStocksByBoutique(@PathVariable Long boutiqueId) {
        return ResponseEntity.ok(boutiqueStockService.getStocksByBoutique(boutiqueId));
    }

    @Operation(summary = "Consulter tous les stocks de toutes les boutiques")
    @GetMapping("/stocks")
    public ResponseEntity<List<BoutiqueStockDTO>> getAllBoutiqueStocks() {
        return ResponseEntity.ok(boutiqueStockService.getAllBoutiqueStocks());
    }

    @Operation(summary = "Effectuer un transfert de stock entre deux entrepôts")
    @PostMapping("/transfer")
    public ResponseEntity<String> transferStock(@RequestBody TransferStockRequest request, Authentication authentication) {
        String email = authentication != null ? authentication.getName() : "admin@system.com";
        boutiqueStockService.transferStock(request, email);
        return ResponseEntity.ok("Transfert de stock effectué avec succès.");
    }
}
