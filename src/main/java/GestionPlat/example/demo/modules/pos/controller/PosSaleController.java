package GestionPlat.example.demo.modules.pos.controller;

import GestionPlat.example.demo.modules.pos.dto.CreatePosSaleRequest;
import GestionPlat.example.demo.modules.pos.dto.PosSaleDTO;
import GestionPlat.example.demo.modules.pos.service.PosSaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pos/sales")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PosSaleController {

    private final PosSaleService posSaleService;

    @PostMapping
    public ResponseEntity<PosSaleDTO> createPosSale(@RequestBody CreatePosSaleRequest request, Authentication authentication) {
        String email = authentication != null ? authentication.getName() : "SYSTEM";
        return ResponseEntity.status(HttpStatus.CREATED).body(posSaleService.createPosSale(request, email));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PosSaleDTO> getPosSaleById(@PathVariable Long id) {
        return ResponseEntity.ok(posSaleService.getPosSaleById(id));
    }

    @GetMapping("/receipt/{receiptNumber}")
    public ResponseEntity<PosSaleDTO> getPosSaleByReceiptNumber(@PathVariable String receiptNumber) {
        return ResponseEntity.ok(posSaleService.getPosSaleByReceiptNumber(receiptNumber));
    }

    @GetMapping("/session/{cashSessionId}")
    public ResponseEntity<List<PosSaleDTO>> getPosSalesBySession(@PathVariable Long cashSessionId) {
        return ResponseEntity.ok(posSaleService.getPosSalesBySession(cashSessionId));
    }

    @GetMapping("/boutique/{boutiqueId}")
    public ResponseEntity<List<PosSaleDTO>> getPosSalesByBoutique(@PathVariable Long boutiqueId) {
        return ResponseEntity.ok(posSaleService.getPosSalesByBoutique(boutiqueId));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    public ResponseEntity<List<PosSaleDTO>> getAllPosSales() {
        return ResponseEntity.ok(posSaleService.getAllPosSales());
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    public ResponseEntity<PosSaleDTO> cancelPosSale(@PathVariable Long id, Authentication authentication) {
        String email = authentication != null ? authentication.getName() : "SYSTEM";
        return ResponseEntity.ok(posSaleService.cancelPosSale(id, email));
    }
}
