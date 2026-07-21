package GestionPlat.example.demo.modules.stock.controller;

import GestionPlat.example.demo.modules.stock.model.StockMovement;
import GestionPlat.example.demo.modules.stock.service.StockMovementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock/movements")
@RequiredArgsConstructor
@Tag(name = "4. Gestion de Stock - Mouvements", description = "Historique et traçabilité des entrées/sorties de stock")
public class StockMovementController {

    private final StockMovementService stockMovementService;

    @Operation(summary = "Historique complet des mouvements de stock")
    @GetMapping
    public ResponseEntity<List<StockMovement>> getAllStockMovements() {
        return ResponseEntity.ok(stockMovementService.getAllStockMovements());
    }

    @Operation(summary = "Historique des mouvements d'un produit spécifique")
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<StockMovement>> getMovementsByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(stockMovementService.getMovementsByProduct(productId));
    }
}
