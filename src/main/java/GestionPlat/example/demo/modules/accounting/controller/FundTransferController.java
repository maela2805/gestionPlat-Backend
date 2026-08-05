package GestionPlat.example.demo.modules.accounting.controller;

import GestionPlat.example.demo.modules.accounting.dto.BoutiqueWalletDTO;
import GestionPlat.example.demo.modules.accounting.dto.CreateFundTransferRequest;
import GestionPlat.example.demo.modules.accounting.dto.FundTransferDTO;
import GestionPlat.example.demo.modules.accounting.dto.RejectFundTransferRequest;
import GestionPlat.example.demo.modules.accounting.model.FundTransferStatus;
import GestionPlat.example.demo.modules.accounting.service.FundTransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounting")
@RequiredArgsConstructor
public class FundTransferController {

    private final FundTransferService fundTransferService;

    @PostMapping("/transfers")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'EMPLOYEE', 'SUPER_ADMIN_ROLE', 'ADMIN_ROLE', 'MANAGER_ROLE', 'EMPLOYEE_ROLE')")
    public ResponseEntity<FundTransferDTO> createFundTransfer(
            @Valid @RequestBody CreateFundTransferRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        FundTransferDTO created = fundTransferService.createFundTransfer(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/transfers")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'EMPLOYEE', 'SUPER_ADMIN_ROLE', 'ADMIN_ROLE', 'MANAGER_ROLE', 'EMPLOYEE_ROLE')")
    public ResponseEntity<List<FundTransferDTO>> getAllFundTransfers(
            @RequestParam(required = false) Long boutiqueId,
            @RequestParam(required = false) FundTransferStatus status) {
        return ResponseEntity.ok(fundTransferService.getAllFundTransfers(boutiqueId, status));
    }

    @GetMapping("/transfers/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'EMPLOYEE', 'SUPER_ADMIN_ROLE', 'ADMIN_ROLE', 'MANAGER_ROLE', 'EMPLOYEE_ROLE')")
    public ResponseEntity<FundTransferDTO> getFundTransferById(@PathVariable Long id) {
        return ResponseEntity.ok(fundTransferService.getFundTransferById(id));
    }

    @PostMapping("/transfers/{id}/approve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'SUPER_ADMIN_ROLE', 'ADMIN_ROLE', 'MANAGER_ROLE')")
    public ResponseEntity<FundTransferDTO> approveFundTransfer(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(fundTransferService.approveFundTransfer(id, userDetails.getUsername()));
    }

    @PostMapping("/transfers/{id}/reject")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'SUPER_ADMIN_ROLE', 'ADMIN_ROLE', 'MANAGER_ROLE')")
    public ResponseEntity<FundTransferDTO> rejectFundTransfer(
            @PathVariable Long id,
            @RequestBody(required = false) RejectFundTransferRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(fundTransferService.rejectFundTransfer(id, request, userDetails.getUsername()));
    }

    @PostMapping("/transfers/{id}/cancel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'EMPLOYEE', 'SUPER_ADMIN_ROLE', 'ADMIN_ROLE', 'MANAGER_ROLE', 'EMPLOYEE_ROLE')")
    public ResponseEntity<FundTransferDTO> cancelFundTransfer(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(fundTransferService.cancelFundTransfer(id, userDetails.getUsername()));
    }

    @GetMapping("/wallet/{boutiqueId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'EMPLOYEE', 'SUPER_ADMIN_ROLE', 'ADMIN_ROLE', 'MANAGER_ROLE', 'EMPLOYEE_ROLE')")
    public ResponseEntity<BoutiqueWalletDTO> getBoutiqueWalletSummary(@PathVariable Long boutiqueId) {
        return ResponseEntity.ok(fundTransferService.getBoutiqueWalletSummary(boutiqueId));
    }
}
