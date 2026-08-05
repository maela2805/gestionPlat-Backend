package GestionPlat.example.demo.modules.stock.controller;

import GestionPlat.example.demo.modules.stock.dto.CreateStockReturnRequest;
import GestionPlat.example.demo.modules.stock.dto.RejectStockReturnRequest;
import GestionPlat.example.demo.modules.stock.dto.StockReturnDTO;
import GestionPlat.example.demo.modules.stock.model.StockReturnStatus;
import GestionPlat.example.demo.modules.stock.service.StockReturnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock/returns")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Retours et Casses", description = "Gestion des déclarations de casse, détérioration et retours d'articles")
public class StockReturnController {

    private final StockReturnService stockReturnService;

    @Operation(summary = "Liste de toutes les déclarations de retour/casse (filtrables par boutique et statut)")
    @GetMapping
    public ResponseEntity<List<StockReturnDTO>> getAllStockReturns(
            @RequestParam(required = false) Long boutiqueId,
            @RequestParam(required = false) StockReturnStatus status) {
        return ResponseEntity.ok(stockReturnService.getAllStockReturns(boutiqueId, status));
    }

    @Operation(summary = "Obtenir une déclaration par son ID")
    @GetMapping("/{id}")
    public ResponseEntity<StockReturnDTO> getStockReturnById(@PathVariable Long id) {
        return ResponseEntity.ok(stockReturnService.getStockReturnById(id));
    }

    @Operation(summary = "Créer une déclaration de retour ou casse d'article")
    @PostMapping
    public ResponseEntity<StockReturnDTO> createStockReturn(
            @Valid @RequestBody CreateStockReturnRequest request,
            Authentication authentication) {
        String email = authentication != null ? authentication.getName() : "SYSTEM";
        return ResponseEntity.status(HttpStatus.CREATED).body(stockReturnService.createStockReturn(request, email));
    }

    @Operation(summary = "Approuver une déclaration et déduire du stock de la boutique")
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    public ResponseEntity<StockReturnDTO> approveStockReturn(@PathVariable Long id, Authentication authentication) {
        String email = authentication != null ? authentication.getName() : "SYSTEM";
        return ResponseEntity.ok(stockReturnService.approveStockReturn(id, email));
    }

    @Operation(summary = "Rejeter une déclaration de casse")
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    public ResponseEntity<StockReturnDTO> rejectStockReturn(
            @PathVariable Long id,
            @RequestBody(required = false) RejectStockReturnRequest request,
            Authentication authentication) {
        String email = authentication != null ? authentication.getName() : "SYSTEM";
        String reason = request != null ? request.getReason() : null;
        return ResponseEntity.ok(stockReturnService.rejectStockReturn(id, reason, email));
    }

    @Operation(summary = "Annuler une déclaration par l'employé/boutique")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<StockReturnDTO> cancelStockReturn(@PathVariable Long id, Authentication authentication) {
        String email = authentication != null ? authentication.getName() : "SYSTEM";
        return ResponseEntity.ok(stockReturnService.cancelStockReturn(id, email));
    }
}
