package GestionPlat.example.demo.modules.caisse.controller;

import GestionPlat.example.demo.modules.caisse.dto.*;
import GestionPlat.example.demo.modules.caisse.service.CashSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/caisse/sessions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CashSessionController {

    private final CashSessionService cashSessionService;

    @PostMapping("/open")
    public ResponseEntity<CashSessionDTO> openSession(@RequestBody OpenCashSessionRequest request, Authentication authentication) {
        String email = authentication != null ? authentication.getName() : "SYSTEM";
        return ResponseEntity.status(HttpStatus.CREATED).body(cashSessionService.openSession(request, email));
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<CashSessionDTO> closeSession(@PathVariable Long id, @RequestBody CloseCashSessionRequest request, Authentication authentication) {
        String email = authentication != null ? authentication.getName() : "SYSTEM";
        return ResponseEntity.ok(cashSessionService.closeSession(id, request, email));
    }

    @GetMapping("/current/user")
    public ResponseEntity<CashSessionDTO> getCurrentUserSession(Authentication authentication) {
        String email = authentication != null ? authentication.getName() : "SYSTEM";
        CashSessionDTO session = cashSessionService.getCurrentSessionForUser(email);
        return session != null ? ResponseEntity.ok(session) : ResponseEntity.noContent().build();
    }

    @GetMapping("/current/boutique/{boutiqueId}")
    public ResponseEntity<CashSessionDTO> getCurrentBoutiqueSession(@PathVariable Long boutiqueId) {
        CashSessionDTO session = cashSessionService.getCurrentSessionForBoutique(boutiqueId);
        return session != null ? ResponseEntity.ok(session) : ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CashSessionDTO> getSessionById(@PathVariable Long id) {
        return ResponseEntity.ok(cashSessionService.getSessionById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    public ResponseEntity<List<CashSessionDTO>> getAllSessions() {
        return ResponseEntity.ok(cashSessionService.getAllSessions());
    }

    @GetMapping("/boutique/{boutiqueId}")
    public ResponseEntity<List<CashSessionDTO>> getSessionsByBoutique(@PathVariable Long boutiqueId) {
        return ResponseEntity.ok(cashSessionService.getSessionsByBoutique(boutiqueId));
    }

    @PostMapping("/{id}/movements")
    public ResponseEntity<CashMovementDTO> addMovement(@PathVariable Long id, @RequestBody CreateCashMovementRequest request, Authentication authentication) {
        String email = authentication != null ? authentication.getName() : "SYSTEM";
        return ResponseEntity.status(HttpStatus.CREATED).body(cashSessionService.addMovement(id, request, email));
    }
}
