package GestionPlat.example.demo.modules.billing.controller;

import GestionPlat.example.demo.modules.billing.dto.*;
import GestionPlat.example.demo.modules.billing.model.InvoiceStatus;
import GestionPlat.example.demo.modules.billing.model.InvoiceType;
import GestionPlat.example.demo.modules.billing.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping
    public ResponseEntity<List<InvoiceDTO>> getAllInvoices(
            @RequestParam(required = false) InvoiceType type,
            @RequestParam(required = false) InvoiceStatus status) {
        return ResponseEntity.ok(invoiceService.getAllInvoices(type, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDTO> getInvoiceById(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.getInvoiceById(id));
    }

    @PostMapping
    public ResponseEntity<InvoiceDTO> createInvoice(@Valid @RequestBody CreateInvoiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(invoiceService.createInvoice(request));
    }

    @PostMapping("/from-sale/{saleId}")
    public ResponseEntity<InvoiceDTO> createInvoiceFromSale(@PathVariable Long saleId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(invoiceService.createInvoiceFromSale(saleId));
    }

    @PostMapping("/from-purchase/{purchaseId}")
    public ResponseEntity<InvoiceDTO> createInvoiceFromPurchaseOrder(@PathVariable Long purchaseId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(invoiceService.createInvoiceFromPurchaseOrder(purchaseId));
    }

    @PutMapping("/{id}/validate")
    public ResponseEntity<InvoiceDTO> validateInvoice(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.validateInvoice(id));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<InvoiceDTO> cancelInvoice(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.cancelInvoice(id));
    }

    @PostMapping("/payments")
    public ResponseEntity<PaymentDTO> addPayment(@Valid @RequestBody CreatePaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(invoiceService.addPayment(request));
    }

    @GetMapping("/{id}/payments")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByInvoiceId(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.getPaymentsByInvoiceId(id));
    }
}
