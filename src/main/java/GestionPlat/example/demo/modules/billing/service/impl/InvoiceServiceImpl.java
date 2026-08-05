package GestionPlat.example.demo.modules.billing.service.impl;

import GestionPlat.example.demo.modules.accounting.service.AccountingService;
import GestionPlat.example.demo.modules.billing.dto.*;
import GestionPlat.example.demo.modules.billing.model.*;
import GestionPlat.example.demo.modules.billing.repository.InvoiceRepository;
import GestionPlat.example.demo.modules.billing.repository.PaymentRepository;
import GestionPlat.example.demo.modules.billing.service.InvoiceService;
import GestionPlat.example.demo.modules.boutique.model.Boutique;
import GestionPlat.example.demo.modules.boutique.repository.BoutiqueRepository;
import GestionPlat.example.demo.modules.purchase.model.PurchaseOrder;
import GestionPlat.example.demo.modules.purchase.model.PurchaseOrderItem;
import GestionPlat.example.demo.modules.purchase.repository.PurchaseOrderRepository;
import GestionPlat.example.demo.modules.sale.model.StoreSale;
import GestionPlat.example.demo.modules.sale.model.StoreSaleItem;
import GestionPlat.example.demo.modules.sale.repository.StoreSaleRepository;
import GestionPlat.example.demo.modules.tiers.model.Tiers;
import GestionPlat.example.demo.modules.tiers.repository.TiersRepository;
import GestionPlat.example.demo.modules.stock.dto.TransferStockRequest;
import GestionPlat.example.demo.modules.stock.service.BoutiqueStockService;
import GestionPlat.example.demo.modules.boutique.model.BoutiqueOrder;
import GestionPlat.example.demo.modules.boutique.model.BoutiqueOrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final TiersRepository tiersRepository;
    private final BoutiqueRepository boutiqueRepository;
    private final StoreSaleRepository storeSaleRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final AccountingService accountingService;
    private final BoutiqueStockService boutiqueStockService;

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceDTO> getAllInvoices(InvoiceType type, InvoiceStatus status) {
        List<Invoice> list;
        if (type != null && status != null) {
            list = invoiceRepository.findByTypeAndStatus(type, status);
        } else if (type != null) {
            list = invoiceRepository.findByType(type);
        } else if (status != null) {
            list = invoiceRepository.findByStatus(status);
        } else {
            list = invoiceRepository.findAll();
        }
        return list.stream()
                .sorted((a, b) -> {
                    LocalDateTime dateA = a.getInvoiceDate() != null ? a.getInvoiceDate() : a.getCreatedAt();
                    LocalDateTime dateB = b.getInvoiceDate() != null ? b.getInvoiceDate() : b.getCreatedAt();
                    if (dateA == null && dateB == null) {
                        return Long.compare(b.getId() != null ? b.getId() : 0, a.getId() != null ? a.getId() : 0);
                    }
                    if (dateA == null) return 1;
                    if (dateB == null) return -1;
                    return dateB.compareTo(dateA);
                })
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceDTO getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facture introuvable avec l'ID : " + id));
        return mapToDTO(invoice);
    }

    @Override
    @Transactional
    public InvoiceDTO createInvoice(CreateInvoiceRequest request) {
        String prefix = request.getType() == InvoiceType.VENTE ? "FAC-V-" : "FAC-A-";
        String number = prefix + (System.currentTimeMillis() % 1000000);

        Tiers tiers = null;
        if (request.getTiersId() != null) {
            tiers = tiersRepository.findById(request.getTiersId()).orElse(null);
        }

        Boutique boutique = null;
        if (request.getBoutiqueId() != null) {
            boutique = boutiqueRepository.findById(request.getBoutiqueId()).orElse(null);
        }

        BigDecimal taxRate = request.getTaxRate() != null ? request.getTaxRate() : BigDecimal.ZERO;
        LocalDateTime invDate = request.getInvoiceDate() != null ? request.getInvoiceDate() : LocalDateTime.now();

        Invoice invoice = Invoice.builder()
                .invoiceNumber(number)
                .type(request.getType())
                .status(InvoiceStatus.BROUILLON)
                .invoiceDate(invDate)
                .dueDate(request.getDueDate())
                .tiers(tiers)
                .boutique(boutique)
                .taxRate(taxRate)
                .note(request.getNote())
                .subtotalHt(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .totalTtc(BigDecimal.ZERO)
                .paidAmount(BigDecimal.ZERO)
                .remainingAmount(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .payments(new ArrayList<>())
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (CreateInvoiceItemRequest itemReq : request.getItems()) {
                BigDecimal qty = itemReq.getQuantity() != null ? itemReq.getQuantity() : BigDecimal.ONE;
                BigDecimal unitPrice = itemReq.getUnitPriceHt() != null ? itemReq.getUnitPriceHt() : BigDecimal.ZERO;
                BigDecimal itemTaxRate = itemReq.getTaxRate() != null ? itemReq.getTaxRate() : taxRate;

                BigDecimal totalHt = qty.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
                BigDecimal tax = totalHt.multiply(itemTaxRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                BigDecimal totalTtc = totalHt.add(tax);

                subtotal = subtotal.add(totalHt);

                InvoiceItem item = InvoiceItem.builder()
                        .invoice(invoice)
                        .description(itemReq.getDescription())
                        .quantity(qty)
                        .unitPriceHt(unitPrice)
                        .taxRate(itemTaxRate)
                        .totalHt(totalHt)
                        .totalTtc(totalTtc)
                        .build();

                invoice.getItems().add(item);
            }
        }

        BigDecimal taxAmount = subtotal.multiply(taxRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal totalTtc = subtotal.add(taxAmount);

        invoice.setSubtotalHt(subtotal);
        invoice.setTaxAmount(taxAmount);
        invoice.setTotalTtc(totalTtc);
        invoice.setRemainingAmount(totalTtc);

        Invoice saved = invoiceRepository.save(invoice);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public InvoiceDTO createInvoiceFromSale(Long saleId) {
        StoreSale sale = storeSaleRepository.findById(saleId)
                .orElseThrow(() -> new RuntimeException("Vente introuvable avec l'ID : " + saleId));

        String number = "FAC-V-" + sale.getReference();

        if (invoiceRepository.existsByInvoiceNumber(number)) {
            return mapToDTO(invoiceRepository.findByInvoiceNumber(number).get());
        }

        Invoice invoice = Invoice.builder()
                .invoiceNumber(number)
                .type(InvoiceType.VENTE)
                .status(InvoiceStatus.VALIDEE)
                .invoiceDate(sale.getSaleDate() != null ? sale.getSaleDate() : LocalDateTime.now())
                .boutique(sale.getBoutique())
                .sourceSale(sale)
                .subtotalHt(sale.getTotalAmount())
                .taxRate(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .totalTtc(sale.getTotalAmount())
                .paidAmount(BigDecimal.ZERO)
                .remainingAmount(sale.getTotalAmount())
                .status(InvoiceStatus.VALIDEE)
                .note("Générée automatiquement depuis la vente " + sale.getReference())
                .items(new ArrayList<>())
                .payments(new ArrayList<>())
                .build();

        for (StoreSaleItem saleItem : sale.getItems()) {
            BigDecimal qty = BigDecimal.valueOf(saleItem.getQuantity());
            BigDecimal unitPrice = saleItem.getWholesalePrice() != null ? saleItem.getWholesalePrice() : BigDecimal.ZERO;
            BigDecimal total = saleItem.getTotalPrice() != null ? saleItem.getTotalPrice() : BigDecimal.ZERO;

            InvoiceItem item = InvoiceItem.builder()
                    .invoice(invoice)
                    .description(saleItem.getProduct() != null ? saleItem.getProduct().getName() : "Produit")
                    .quantity(qty)
                    .unitPriceHt(unitPrice)
                    .taxRate(BigDecimal.ZERO)
                    .totalHt(total)
                    .totalTtc(total)
                    .build();

            invoice.getItems().add(item);
        }

        Invoice saved = invoiceRepository.save(invoice);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public InvoiceDTO createInvoiceFromPurchaseOrder(Long purchaseOrderId) {
        PurchaseOrder po = purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new RuntimeException("Commande fournisseur introuvable avec l'ID : " + purchaseOrderId));

        String number = "FAC-A-" + po.getReference();

        if (invoiceRepository.existsByInvoiceNumber(number)) {
            return mapToDTO(invoiceRepository.findByInvoiceNumber(number).get());
        }

        Invoice invoice = Invoice.builder()
                .invoiceNumber(number)
                .type(InvoiceType.ACHAT)
                .status(InvoiceStatus.VALIDEE)
                .invoiceDate(po.getOrderDate() != null ? po.getOrderDate() : LocalDateTime.now())
                .dueDate(po.getExpectedDeliveryDate())
                .tiers(po.getSupplier())
                .sourcePurchaseOrder(po)
                .subtotalHt(po.getTotalAmount())
                .taxRate(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .totalTtc(po.getTotalAmount())
                .paidAmount(BigDecimal.ZERO)
                .remainingAmount(po.getTotalAmount())
                .note("Générée automatiquement depuis la commande d'achat " + po.getReference())
                .items(new ArrayList<>())
                .payments(new ArrayList<>())
                .build();

        for (PurchaseOrderItem poItem : po.getItems()) {
            BigDecimal qty = poItem.getQuantityOrdered() != null ? BigDecimal.valueOf(poItem.getQuantityOrdered()) : BigDecimal.ONE;
            BigDecimal unitPrice = poItem.getUnitPrice() != null ? poItem.getUnitPrice() : BigDecimal.ZERO;
            BigDecimal total = poItem.getTotalPrice() != null ? poItem.getTotalPrice() : BigDecimal.ZERO;

            String desc = poItem.getProduct() != null ? poItem.getProduct().getName() : (poItem.getPendingProductName() != null ? poItem.getPendingProductName() : "Produit");

            InvoiceItem item = InvoiceItem.builder()
                    .invoice(invoice)
                    .description(desc)
                    .quantity(qty)
                    .unitPriceHt(unitPrice)
                    .taxRate(BigDecimal.ZERO)
                    .totalHt(total)
                    .totalTtc(total)
                    .build();

            invoice.getItems().add(item);
        }

        Invoice saved = invoiceRepository.save(invoice);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public InvoiceDTO validateInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facture introuvable avec l'ID : " + id));

        if (invoice.getStatus() != InvoiceStatus.BROUILLON) {
            throw new RuntimeException("Seule une facture au statut BROUILLON peut être validée.");
        }

        invoice.setStatus(InvoiceStatus.VALIDEE);
        Invoice updated = invoiceRepository.save(invoice);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public InvoiceDTO cancelInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facture introuvable avec l'ID : " + id));

        invoice.setStatus(InvoiceStatus.ANNULEE);
        Invoice updated = invoiceRepository.save(invoice);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public PaymentDTO addPayment(CreatePaymentRequest request) {
        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new RuntimeException("Facture introuvable avec l'ID : " + request.getInvoiceId()));

        if (invoice.getStatus() == InvoiceStatus.ANNULEE) {
            throw new RuntimeException("Impossible d'ajouter un règlement sur une facture annulée.");
        }

        String regNumber = "REG-" + (System.currentTimeMillis() % 1000000);

        Payment payment = Payment.builder()
                .paymentNumber(regNumber)
                .invoice(invoice)
                .tiers(invoice.getTiers())
                .paymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : LocalDateTime.now())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .reference(request.getReference())
                .note(request.getNote())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        BigDecimal newPaidAmount = invoice.getPaidAmount().add(request.getAmount());
        BigDecimal newRemainingAmount = invoice.getTotalTtc().subtract(newPaidAmount);
        if (newRemainingAmount.compareTo(BigDecimal.ZERO) < 0) {
            newRemainingAmount = BigDecimal.ZERO;
        }

        invoice.setPaidAmount(newPaidAmount);
        invoice.setRemainingAmount(newRemainingAmount);

        if (newRemainingAmount.compareTo(BigDecimal.ZERO) == 0) {
            invoice.setStatus(InvoiceStatus.PAYEE);
        } else {
            invoice.setStatus(InvoiceStatus.PAYEE_PARTIEL);
        }

        invoiceRepository.save(invoice);

        // Enregistrer automatiquement dans le journal comptable
        accountingService.recordPaymentAccountingEntry(savedPayment, invoice);

        return mapToPaymentDTO(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDTO> getPaymentsByInvoiceId(Long invoiceId) {
        return paymentRepository.findByInvoiceId(invoiceId).stream()
                .map(this::mapToPaymentDTO)
                .toList();
    }

    @Override
    @Transactional
    public InvoiceDTO confirmDelivery(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facture introuvable avec l'ID : " + id));

        if (Boolean.TRUE.equals(invoice.getDeliveryConfirmed())) {
            return mapToDTO(invoice);
        }

        invoice.setDeliveryConfirmed(true);
        invoice.setDeliveryDate(LocalDateTime.now());

        if (invoice.getSourceBoutiqueOrder() != null) {
            BoutiqueOrder order = invoice.getSourceBoutiqueOrder();
            order.setStatus(GestionPlat.example.demo.modules.boutique.model.BoutiqueOrderStatus.DELIVERED);

            // Effectuer le transfert de stock réel lors de la confirmation de livraison
            if (order.getItems() != null && order.getBoutique() != null) {
                for (BoutiqueOrderItem item : order.getItems()) {
                    if (item.getProduct() != null && item.getQuantity() != null && item.getQuantity() > 0) {
                        TransferStockRequest stockTransfer = new TransferStockRequest();
                        stockTransfer.setProductId(item.getProduct().getId());
                        stockTransfer.setFromBoutiqueId(null); // Dépôt Central
                        stockTransfer.setToBoutiqueId(order.getBoutique().getId());
                        stockTransfer.setQuantity(item.getQuantity());
                        stockTransfer.setNote("Livraison confirmée pour la facture N° " + invoice.getInvoiceNumber());

                        boutiqueStockService.transferStock(stockTransfer, "System");
                    }
                }
            }
        }

        return mapToDTO(invoiceRepository.save(invoice));
    }

    @Override
    @Transactional
    public InvoiceDTO updateInvoice(Long id, UpdateInvoiceRequest request) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facture introuvable avec l'ID : " + id));

        if (request.getNote() != null) {
            invoice.setNote(request.getNote());
        }
        if (request.getDueDate() != null) {
            invoice.setDueDate(request.getDueDate());
        }
        if (request.getInvoiceDate() != null) {
            invoice.setInvoiceDate(request.getInvoiceDate());
        }
        if (request.getTaxRate() != null) {
            invoice.setTaxRate(request.getTaxRate());
        }
        if (request.getTiersId() != null) {
            Tiers tiers = tiersRepository.findById(request.getTiersId()).orElse(null);
            invoice.setTiers(tiers);
        }
        if (request.getPaidAmount() != null) {
            invoice.setPaidAmount(request.getPaidAmount());
        }
        if (request.getStatus() != null) {
            invoice.setStatus(request.getStatus());
        }
        if (request.getDeliveryDate() != null) {
            invoice.setDeliveryDate(request.getDeliveryDate());
        }
        if (request.getDriverName() != null) {
            invoice.setDriverName(request.getDriverName());
        }
        if (request.getDriverPhone() != null) {
            invoice.setDriverPhone(request.getDriverPhone());
        }
        if (request.getVehicleRegistration() != null) {
            invoice.setVehicleRegistration(request.getVehicleRegistration());
        }
        if (request.getAttachmentUrl() != null) {
            invoice.setAttachmentUrl(request.getAttachmentUrl());
        }

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            invoice.getItems().clear();
            BigDecimal subtotal = BigDecimal.ZERO;
            BigDecimal taxRate = invoice.getTaxRate() != null ? invoice.getTaxRate() : BigDecimal.ZERO;

            for (CreateInvoiceItemRequest itemReq : request.getItems()) {
                BigDecimal qty = itemReq.getQuantity() != null ? itemReq.getQuantity() : BigDecimal.ONE;
                BigDecimal unitPrice = itemReq.getUnitPriceHt() != null ? itemReq.getUnitPriceHt() : BigDecimal.ZERO;
                BigDecimal itemTaxRate = itemReq.getTaxRate() != null ? itemReq.getTaxRate() : taxRate;

                BigDecimal totalHt = qty.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
                BigDecimal tax = totalHt.multiply(itemTaxRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                BigDecimal totalTtc = totalHt.add(tax);

                subtotal = subtotal.add(totalHt);

                InvoiceItem item = InvoiceItem.builder()
                        .invoice(invoice)
                        .description(itemReq.getDescription())
                        .quantity(qty)
                        .unitPriceHt(unitPrice)
                        .taxRate(itemTaxRate)
                        .totalHt(totalHt)
                        .totalTtc(totalTtc)
                        .build();

                invoice.getItems().add(item);
            }

            BigDecimal taxAmount = subtotal.multiply(taxRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal totalTtc = subtotal.add(taxAmount);

            invoice.setSubtotalHt(subtotal);
            invoice.setTaxAmount(taxAmount);
            invoice.setTotalTtc(totalTtc);
        }

        BigDecimal totalTtc = invoice.getTotalTtc() != null ? invoice.getTotalTtc() : BigDecimal.ZERO;
        BigDecimal paid = invoice.getPaidAmount() != null ? invoice.getPaidAmount() : BigDecimal.ZERO;
        BigDecimal remaining = totalTtc.subtract(paid);
        if (remaining.compareTo(BigDecimal.ZERO) < 0) remaining = BigDecimal.ZERO;
        invoice.setRemainingAmount(remaining);

        if (request.getStatus() == null) {
            if (paid.compareTo(BigDecimal.ZERO) == 0) {
                invoice.setStatus(InvoiceStatus.VALIDEE);
            } else if (remaining.compareTo(BigDecimal.ZERO) == 0) {
                invoice.setStatus(InvoiceStatus.PAYEE);
            } else {
                invoice.setStatus(InvoiceStatus.PAYEE_PARTIEL);
            }
        }

        return mapToDTO(invoiceRepository.save(invoice));
    }

    private InvoiceDTO mapToDTO(Invoice invoice) {
        List<InvoiceItemDTO> itemDTOs = invoice.getItems() != null ? invoice.getItems().stream().map(this::mapToItemDTO).toList() : List.of();
        List<PaymentDTO> paymentDTOs = invoice.getPayments() != null ? invoice.getPayments().stream().map(this::mapToPaymentDTO).toList() : List.of();

        return InvoiceDTO.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .type(invoice.getType())
                .status(invoice.getStatus())
                .invoiceDate(invoice.getInvoiceDate())
                .dueDate(invoice.getDueDate())
                .tiersId(invoice.getTiers() != null ? invoice.getTiers().getId() : null)
                .tiersName(invoice.getTiers() != null ? invoice.getTiers().getName() : null)
                .tiersCode(invoice.getTiers() != null ? invoice.getTiers().getCode() : null)
                .boutiqueId(invoice.getBoutique() != null ? invoice.getBoutique().getId() : null)
                .boutiqueName(invoice.getBoutique() != null ? invoice.getBoutique().getName() : null)
                .sourceSaleId(invoice.getSourceSale() != null ? invoice.getSourceSale().getId() : null)
                .sourcePurchaseOrderId(invoice.getSourcePurchaseOrder() != null ? invoice.getSourcePurchaseOrder().getId() : null)
                .subtotalHt(invoice.getSubtotalHt())
                .taxRate(invoice.getTaxRate())
                .taxAmount(invoice.getTaxAmount())
                .totalTtc(invoice.getTotalTtc())
                .paidAmount(invoice.getPaidAmount())
                .remainingAmount(invoice.getRemainingAmount())
                .note(invoice.getNote())
                .deliveryConfirmed(invoice.getDeliveryConfirmed())
                .deliveryDate(invoice.getDeliveryDate())
                .driverName(invoice.getDriverName())
                .driverPhone(invoice.getDriverPhone())
                .vehicleRegistration(invoice.getVehicleRegistration())
                .attachmentUrl(invoice.getAttachmentUrl())
                .items(itemDTOs)
                .payments(paymentDTOs)
                .createdAt(invoice.getCreatedAt())
                .updatedAt(invoice.getUpdatedAt())
                .build();
    }

    private InvoiceItemDTO mapToItemDTO(InvoiceItem item) {
        return InvoiceItemDTO.builder()
                .id(item.getId())
                .description(item.getDescription())
                .quantity(item.getQuantity())
                .unitPriceHt(item.getUnitPriceHt())
                .taxRate(item.getTaxRate())
                .totalHt(item.getTotalHt())
                .totalTtc(item.getTotalTtc())
                .build();
    }

    private PaymentDTO mapToPaymentDTO(Payment payment) {
        return PaymentDTO.builder()
                .id(payment.getId())
                .paymentNumber(payment.getPaymentNumber())
                .invoiceId(payment.getInvoice() != null ? payment.getInvoice().getId() : null)
                .invoiceNumber(payment.getInvoice() != null ? payment.getInvoice().getInvoiceNumber() : null)
                .tiersId(payment.getTiers() != null ? payment.getTiers().getId() : null)
                .tiersName(payment.getTiers() != null ? payment.getTiers().getName() : null)
                .paymentDate(payment.getPaymentDate())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .reference(payment.getReference())
                .note(payment.getNote())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
