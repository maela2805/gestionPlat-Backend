package GestionPlat.example.demo.modules.purchase.controller;

import GestionPlat.example.demo.modules.purchase.dto.CreatePurchaseOrderRequest;
import GestionPlat.example.demo.modules.purchase.dto.PurchaseOrderDTO;
import GestionPlat.example.demo.modules.purchase.service.PurchaseOrderService;
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
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
@Tag(name = "Commandes Fournisseurs & Approvisionnement", description = "Passation et suivi des bons de commande fournisseurs")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @Operation(summary = "Liste de toutes les commandes fournisseurs")
    @GetMapping
    public ResponseEntity<List<PurchaseOrderDTO>> getAllPurchaseOrders() {
        return ResponseEntity.ok(purchaseOrderService.getAllPurchaseOrders());
    }

    @Operation(summary = "Obtenir les détails d'une commande fournisseur")
    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrderDTO> getPurchaseOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.getPurchaseOrderById(id));
    }

    @Operation(summary = "Créer une nouvelle commande fournisseur")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_MANAGER', 'WRITE_STOCK')")
    public ResponseEntity<PurchaseOrderDTO> createPurchaseOrder(@Valid @RequestBody CreatePurchaseOrderRequest request) {
        return ResponseEntity.ok(purchaseOrderService.createPurchaseOrder(request));
    }

    @Operation(summary = "Valider une commande fournisseur")
    @PutMapping("/{id}/validate")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_MANAGER', 'WRITE_STOCK')")
    public ResponseEntity<PurchaseOrderDTO> validatePurchaseOrder(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.validatePurchaseOrder(id));
    }

    @Operation(summary = "Réceptionner la livraison d'une commande (Incrémente le stock central)")
    @PutMapping("/{id}/receive")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_MANAGER', 'WRITE_STOCK')")
    public ResponseEntity<PurchaseOrderDTO> receiveDelivery(@PathVariable Long id, Authentication authentication) {
        String userEmail = authentication != null ? authentication.getName() : "SYSTEM";
        return ResponseEntity.ok(purchaseOrderService.receiveDelivery(id, userEmail));
    }

    @Operation(summary = "Annuler une commande fournisseur")
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_MANAGER', 'WRITE_STOCK')")
    public ResponseEntity<PurchaseOrderDTO> cancelPurchaseOrder(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.cancelPurchaseOrder(id));
    }
}
