package GestionPlat.example.demo.modules.boutique.controller;

import GestionPlat.example.demo.modules.billing.dto.InvoiceDTO;
import GestionPlat.example.demo.modules.boutique.dto.ApproveBoutiqueOrderRequest;
import GestionPlat.example.demo.modules.boutique.dto.BoutiqueOrderDTO;
import GestionPlat.example.demo.modules.boutique.dto.CreateBoutiqueOrderRequest;
import GestionPlat.example.demo.modules.boutique.service.BoutiqueOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boutique-orders")
@RequiredArgsConstructor
@Tag(name = "Commandes Inter-Boutiques", description = "Gestion des commandes d'approvisionnement des boutiques au dépôt principal")
public class BoutiqueOrderController {

    private final BoutiqueOrderService boutiqueOrderService;

    @Operation(summary = "Liste de toutes les commandes inter-boutiques (Vue Admin)")
    @GetMapping
    public ResponseEntity<List<BoutiqueOrderDTO>> getAllOrders() {
        return ResponseEntity.ok(boutiqueOrderService.getAllOrders());
    }

    @Operation(summary = "Liste des commandes d'une boutique spécifique")
    @GetMapping("/boutique/{boutiqueId}")
    public ResponseEntity<List<BoutiqueOrderDTO>> getOrdersByBoutique(@PathVariable Long boutiqueId) {
        return ResponseEntity.ok(boutiqueOrderService.getOrdersByBoutique(boutiqueId));
    }

    @Operation(summary = "Obtenir les détails d'une commande inter-boutique par son ID")
    @GetMapping("/{id}")
    public ResponseEntity<BoutiqueOrderDTO> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(boutiqueOrderService.getOrderById(id));
    }

    @Operation(summary = "Créer une nouvelle commande d'approvisionnement pour une boutique")
    @PostMapping
    public ResponseEntity<BoutiqueOrderDTO> createOrder(@RequestBody CreateBoutiqueOrderRequest request,
                                                        Authentication authentication) {
        String email = authentication != null ? authentication.getName() : "user@boutique.com";
        return ResponseEntity.ok(boutiqueOrderService.createOrder(request, email));
    }

    @Operation(summary = "Approuver / Modifier et Approuver une commande avec données de livraison (ADMIN)")
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'SUPER_ADMIN', 'ROLE_ADMIN', 'ADMIN', 'ROLE_MANAGER', 'MANAGER', 'WRITE_STOCK')")
    public ResponseEntity<BoutiqueOrderDTO> approveOrder(@PathVariable Long id,
                                                         @RequestBody ApproveBoutiqueOrderRequest request,
                                                         Authentication authentication) {
        String email = authentication != null ? authentication.getName() : "admin@system.com";
        return ResponseEntity.ok(boutiqueOrderService.approveOrder(id, request, email));
    }

    @Operation(summary = "Refuser une commande inter-boutique (ADMIN)")
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'SUPER_ADMIN', 'ROLE_ADMIN', 'ADMIN', 'ROLE_MANAGER', 'MANAGER', 'WRITE_STOCK')")
    public ResponseEntity<BoutiqueOrderDTO> rejectOrder(@PathVariable Long id,
                                                        @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(boutiqueOrderService.rejectOrder(id, reason));
    }

    @Operation(summary = "Annuler une commande inter-boutique")
    @PutMapping("/{id}/cancel")
    public ResponseEntity<BoutiqueOrderDTO> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(boutiqueOrderService.cancelOrder(id));
    }

    @Operation(summary = "Supprimer une commande inter-boutique (ADMIN)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'SUPER_ADMIN', 'ROLE_ADMIN', 'ADMIN')")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        boutiqueOrderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Générer la facture et le bon de livraison pour une commande")
    @PostMapping("/{id}/create-invoice")
    public ResponseEntity<InvoiceDTO> createInvoiceForOrder(@PathVariable Long id) {
        return ResponseEntity.ok(boutiqueOrderService.createInvoiceForOrder(id));
    }
}
