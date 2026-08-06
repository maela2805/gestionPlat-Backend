package GestionPlat.example.demo.modules.accounting.service.impl;

import GestionPlat.example.demo.modules.accounting.dto.BoutiqueWalletDTO;
import GestionPlat.example.demo.modules.accounting.dto.CreateFundTransferRequest;
import GestionPlat.example.demo.modules.accounting.dto.FundTransferDTO;
import GestionPlat.example.demo.modules.accounting.dto.RejectFundTransferRequest;
import GestionPlat.example.demo.modules.accounting.model.*;
import GestionPlat.example.demo.modules.accounting.repository.AccountingEntryRepository;
import GestionPlat.example.demo.modules.accounting.repository.FundTransferRepository;
import GestionPlat.example.demo.modules.accounting.service.FundTransferService;
import GestionPlat.example.demo.modules.auth.model.User;
import GestionPlat.example.demo.modules.auth.repository.UserRepository;
import GestionPlat.example.demo.modules.billing.model.Invoice;
import GestionPlat.example.demo.modules.billing.model.InvoiceStatus;
import GestionPlat.example.demo.modules.billing.model.InvoiceType;
import GestionPlat.example.demo.modules.billing.model.Payment;
import GestionPlat.example.demo.modules.billing.model.PaymentMethod;
import GestionPlat.example.demo.modules.billing.repository.InvoiceRepository;
import GestionPlat.example.demo.modules.billing.repository.PaymentRepository;
import GestionPlat.example.demo.modules.boutique.model.Boutique;
import GestionPlat.example.demo.modules.boutique.repository.BoutiqueRepository;
import GestionPlat.example.demo.modules.pos.repository.PosSaleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FundTransferServiceImpl implements FundTransferService {

    private final FundTransferRepository fundTransferRepository;
    private final BoutiqueRepository boutiqueRepository;
    private final UserRepository userRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final AccountingEntryRepository accountingEntryRepository;
    private final GestionPlat.example.demo.modules.caisse.repository.CashSessionRepository cashSessionRepository;

    private final PosSaleRepository posSaleRepository;

    @Override
    @Transactional
    public FundTransferDTO createFundTransfer(CreateFundTransferRequest request, String userEmail) {
        User user = (userEmail != null) ? userRepository.findByEmail(userEmail).orElse(null) : null;

        Long targetBoutiqueId = request.getBoutiqueId();
        if (targetBoutiqueId == null) {
            targetBoutiqueId = user.getBoutique() != null ? user.getBoutique().getId() : null;
        }

        if (targetBoutiqueId == null) {
            throw new IllegalArgumentException("Veuillez spécifier la boutique concernée par le versement.");
        }

        Boutique boutique = boutiqueRepository.findById(targetBoutiqueId)
                .orElseThrow(() -> new RuntimeException("Boutique introuvable"));

        // Contrôle du solde disponible en caisse globale boutique
        BigDecimal totalCashSales = posSaleRepository.sumCashSalesByBoutiqueId(boutique.getId());
        BigDecimal totalTransferred = fundTransferRepository.sumTransferredAmountByBoutiqueId(boutique.getId());
        BigDecimal soldeCaisseBoutique = totalCashSales.subtract(totalTransferred);
        if (soldeCaisseBoutique.compareTo(BigDecimal.ZERO) < 0) soldeCaisseBoutique = BigDecimal.ZERO;

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Veuillez saisir un montant de versement valide.");
        }

        if (request.getAmount().compareTo(soldeCaisseBoutique) > 0) {
            throw new IllegalArgumentException("Impossible d'effectuer un versement de " 
                    + String.format("%,.0f", request.getAmount()) + " FCFA. Le solde disponible dans la caisse globale de la boutique est seulement de " 
                    + String.format("%,.0f", soldeCaisseBoutique) + " FCFA.");
        }

        String reference = "TRF-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        GestionPlat.example.demo.modules.caisse.model.CashSession cashSession = null;
        if (request.getCashSessionId() != null) {
            cashSession = cashSessionRepository.findById(request.getCashSessionId()).orElse(null);
        }
        if (cashSession == null) {
            cashSession = cashSessionRepository.findByBoutiqueIdAndStatus(boutique.getId(), GestionPlat.example.demo.modules.caisse.model.CashSessionStatus.OPEN).orElse(null);
        }

        VersementType versemenType = request.getVersemenType() != null 
                ? request.getVersemenType() : VersementType.VERSEMENT_RECETTE;

        Invoice invoice = null;
        if (request.getInvoiceId() != null) {
            invoice = invoiceRepository.findById(request.getInvoiceId()).orElse(null);
            if (invoice != null) {
                versemenType = VersementType.VERSEMENT_FACTURE;
            }
        }

        FundTransfer transfer = FundTransfer.builder()
                .reference(reference)
                .boutique(boutique)
                .cashSession(cashSession)
                .versemenType(versemenType)
                .invoice(invoice)
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : PaymentMethod.ESPECES)
                .proofUrl(request.getProofUrl())
                .notes(request.getNotes())
                .userEmail(userEmail)
                .status(FundTransferStatus.PENDING)
                .build();

        FundTransfer saved = fundTransferRepository.save(transfer);

        if (cashSession != null) {
            BigDecimal totalTrf = fundTransferRepository.sumAmountByCashSessionId(cashSession.getId());
            cashSession.setTotalTransferredAmount(totalTrf);
            cashSessionRepository.save(cashSession);
        }

        log.info("Versement de fonds créé avec succès ref: {} pour boutique: {}", reference, boutique.getName());
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public FundTransferDTO approveFundTransfer(Long id, String userEmail) {
        FundTransfer transfer = fundTransferRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Versement introuvable avec l'ID : " + id));

        if (transfer.getStatus() != FundTransferStatus.PENDING) {
            throw new IllegalStateException("Seuls les versements en attente (PENDING) peuvent être approuvés.");
        }

        transfer.setStatus(FundTransferStatus.APPROVED);
        transfer.setApprovedByEmail(userEmail);
        FundTransfer updated = fundTransferRepository.save(transfer);

        String bName = updated.getBoutique() != null ? updated.getBoutique().getName() : "Boutique";
        PaymentMethod pMethod = updated.getPaymentMethod() != null ? updated.getPaymentMethod() : PaymentMethod.ESPECES;

        BigDecimal transferAmount = updated.getAmount() != null ? updated.getAmount() : BigDecimal.ZERO;

        // 1. Auto-création d'une entrée comptable en recette
        if (transferAmount.compareTo(BigDecimal.ZERO) > 0) {
            String entryCode = "ENT-TRF-" + updated.getId() + "-" + System.currentTimeMillis();
            AccountingEntry entry = AccountingEntry.builder()
                    .entryCode(entryCode)
                    .entryDate(LocalDateTime.now())
                    .type(EntryType.RECETTE)
                    .category(AccountingCategory.AUTRES_PRODUITS)
                    .amount(transferAmount)
                    .paymentMethod(pMethod)
                    .description("Versement recette caisse -> Caisse principale (Boutique " + bName + " - Réf: " + updated.getReference() + ")")
                    .build();
            accountingEntryRepository.save(entry);
            log.info("Écriture comptable générée pour le versement {}", updated.getReference());
        }

        // 2. Imputation du versement sur la facture ciblée ou FIFO
        if (updated.getInvoice() != null && transferAmount.compareTo(BigDecimal.ZERO) > 0) {
            Invoice invoice = updated.getInvoice();
            BigDecimal invoiceTotal = invoice.getTotalTtc() != null ? invoice.getTotalTtc() : BigDecimal.ZERO;
            BigDecimal invoicePaid = invoice.getPaidAmount() != null ? invoice.getPaidAmount() : BigDecimal.ZERO;
            BigDecimal invoiceRemaining = invoice.getRemainingAmount() != null ? invoice.getRemainingAmount() : invoiceTotal.subtract(invoicePaid);

            BigDecimal allocatedAmount = transferAmount.min(invoiceRemaining);
            BigDecimal newPaidAmount = invoicePaid.add(allocatedAmount);
            BigDecimal newRemaining = invoiceTotal.subtract(newPaidAmount);
            if (newRemaining.compareTo(BigDecimal.ZERO) < 0) {
                newRemaining = BigDecimal.ZERO;
            }

            invoice.setPaidAmount(newPaidAmount);
            invoice.setRemainingAmount(newRemaining);

            if (newRemaining.compareTo(BigDecimal.ZERO) == 0) {
                invoice.setStatus(InvoiceStatus.PAYEE);
            } else if (newPaidAmount.compareTo(BigDecimal.ZERO) > 0) {
                invoice.setStatus(InvoiceStatus.PAYEE_PARTIEL);
            }

            Payment payment = Payment.builder()
                    .paymentNumber("PAY-TRF-" + updated.getId() + "-" + System.currentTimeMillis())
                    .invoice(invoice)
                    .tiers(invoice.getTiers())
                    .paymentDate(LocalDateTime.now())
                    .amount(allocatedAmount)
                    .paymentMethod(pMethod)
                    .reference(updated.getReference())
                    .note("Versement ciblé pour Facture N° " + invoice.getInvoiceNumber() + " (Réf: " + updated.getReference() + ")")
                    .build();

            paymentRepository.save(payment);
            invoiceRepository.save(invoice);
            log.info("Versement ciblé {} de {} FCFA appliqué sur la facture {}", updated.getReference(), allocatedAmount, invoice.getInvoiceNumber());

        } else if (updated.getBoutique() != null && transferAmount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal availableAmount = transferAmount;
            List<Invoice> unpaidInvoices = invoiceRepository.findUnpaidCessionInvoicesByBoutiqueId(updated.getBoutique().getId());

            int paymentIdx = 1;
            for (Invoice invoice : unpaidInvoices) {
                if (availableAmount == null || availableAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    break;
                }

                BigDecimal invoiceTotal = invoice.getTotalTtc() != null ? invoice.getTotalTtc() : BigDecimal.ZERO;
                BigDecimal invoicePaid = invoice.getPaidAmount() != null ? invoice.getPaidAmount() : BigDecimal.ZERO;
                BigDecimal invoiceRemaining = invoice.getRemainingAmount() != null ? invoice.getRemainingAmount() : invoiceTotal.subtract(invoicePaid);

                if (invoiceRemaining.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }

                BigDecimal allocatedAmount = availableAmount.min(invoiceRemaining);

                BigDecimal newPaidAmount = invoicePaid.add(allocatedAmount);
                BigDecimal newRemaining = invoiceTotal.subtract(newPaidAmount);
                if (newRemaining.compareTo(BigDecimal.ZERO) < 0) {
                    newRemaining = BigDecimal.ZERO;
                }

                invoice.setPaidAmount(newPaidAmount);
                invoice.setRemainingAmount(newRemaining);

                if (newRemaining.compareTo(BigDecimal.ZERO) == 0) {
                    invoice.setStatus(InvoiceStatus.PAYEE);
                } else if (newPaidAmount.compareTo(BigDecimal.ZERO) > 0) {
                    invoice.setStatus(InvoiceStatus.PAYEE_PARTIEL);
                }

                // Enregistrement du règlement lié à la facture
                Payment payment = Payment.builder()
                        .paymentNumber("PAY-TRF-" + updated.getId() + "-" + System.currentTimeMillis() + "-" + paymentIdx++)
                        .invoice(invoice)
                        .tiers(invoice.getTiers())
                        .paymentDate(LocalDateTime.now())
                        .amount(allocatedAmount)
                        .paymentMethod(pMethod)
                        .reference(updated.getReference())
                        .note("Règlement versement de fonds boutique " + bName + " (Réf: " + updated.getReference() + ")")
                        .build();

                paymentRepository.save(payment);
                invoiceRepository.save(invoice);

                availableAmount = availableAmount.subtract(allocatedAmount);
                log.info("Versement {} de {} FCFA alloué sur la facture {}", updated.getReference(), allocatedAmount, invoice.getInvoiceNumber());
            }
        }

        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public FundTransferDTO rejectFundTransfer(Long id, RejectFundTransferRequest request, String userEmail) {
        FundTransfer transfer = fundTransferRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Versement introuvable avec l'ID : " + id));

        if (transfer.getStatus() != FundTransferStatus.PENDING) {
            throw new IllegalStateException("Seuls les versements en attente peuvent être rejetés.");
        }

        transfer.setStatus(FundTransferStatus.REJECTED);
        transfer.setApprovedByEmail(userEmail);
        transfer.setRejectionReason(request != null ? request.getReason() : "Rejeté par l'administrateur");
        FundTransfer updated = fundTransferRepository.save(transfer);

        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public FundTransferDTO cancelFundTransfer(Long id, String userEmail) {
        FundTransfer transfer = fundTransferRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Versement introuvable avec l'ID : " + id));

        if (transfer.getStatus() != FundTransferStatus.PENDING) {
            throw new IllegalStateException("Seuls les versements en attente peuvent être annulés.");
        }

        transfer.setStatus(FundTransferStatus.CANCELLED);
        FundTransfer updated = fundTransferRepository.save(transfer);
        return mapToDTO(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public FundTransferDTO getFundTransferById(Long id) {
        return fundTransferRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new RuntimeException("Versement introuvable"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FundTransferDTO> getAllFundTransfers(Long boutiqueId, FundTransferStatus status) {
        List<FundTransfer> list;
        if (boutiqueId != null) {
            list = fundTransferRepository.findByBoutiqueIdOrderByCreatedAtDesc(boutiqueId);
        } else if (status != null) {
            list = fundTransferRepository.findByStatusOrderByCreatedAtDesc(status);
        } else {
            list = fundTransferRepository.findAllByOrderByCreatedAtDesc();
        }

        if (boutiqueId != null && status != null) {
            list = list.stream()
                    .filter(t -> t.getStatus() == status)
                    .collect(Collectors.toList());
        }

        return list.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BoutiqueWalletDTO getBoutiqueWalletSummary(Long boutiqueId) {
        if (boutiqueId == null) {
            BigDecimal totalCession = invoiceRepository.sumAllCessionInvoicesTotalTtc();
            if (totalCession == null) totalCession = BigDecimal.ZERO;

            BigDecimal totalPaid = fundTransferRepository.sumAmountByStatus(FundTransferStatus.APPROVED);
            if (totalPaid == null) totalPaid = BigDecimal.ZERO;

            BigDecimal pendingTransfers = fundTransferRepository.sumAmountByStatus(FundTransferStatus.PENDING);
            if (pendingTransfers == null) pendingTransfers = BigDecimal.ZERO;

            BigDecimal balanceDue = totalCession.subtract(totalPaid);
            if (balanceDue.compareTo(BigDecimal.ZERO) < 0) balanceDue = BigDecimal.ZERO;

            BigDecimal totalCashSales = posSaleRepository.sumAllCashSales();
            BigDecimal totalTransferred = fundTransferRepository.sumAllTransferredAmount();
            BigDecimal availableCash = totalCashSales.subtract(totalTransferred);
            if (availableCash.compareTo(BigDecimal.ZERO) < 0) availableCash = BigDecimal.ZERO;

            return BoutiqueWalletDTO.builder()
                    .boutiqueId(null)
                    .boutiqueName("Toutes les Boutiques (Vue Globale)")
                    .totalCessionInvoicesAmount(totalCession)
                    .totalPaidAmount(totalPaid)
                    .balanceDue(balanceDue)
                    .pendingTransfersAmount(pendingTransfers)
                    .availableCashBalance(availableCash)
                    .build();
        }

        Boutique boutique = boutiqueRepository.findById(boutiqueId)
                .orElseThrow(() -> new RuntimeException("Boutique introuvable"));

        BigDecimal totalCession = invoiceRepository.sumCessionInvoicesTotalTtcByBoutiqueId(boutiqueId);
        if (totalCession == null) totalCession = BigDecimal.ZERO;

        BigDecimal totalPaid = fundTransferRepository.sumAmountByBoutiqueIdAndStatus(boutiqueId, FundTransferStatus.APPROVED);
        if (totalPaid == null) totalPaid = BigDecimal.ZERO;

        BigDecimal pendingTransfers = fundTransferRepository.sumAmountByBoutiqueIdAndStatus(boutiqueId, FundTransferStatus.PENDING);
        if (pendingTransfers == null) pendingTransfers = BigDecimal.ZERO;

        BigDecimal balanceDue = totalCession.subtract(totalPaid);
        if (balanceDue.compareTo(BigDecimal.ZERO) < 0) balanceDue = BigDecimal.ZERO;

        BigDecimal totalCashSales = posSaleRepository.sumCashSalesByBoutiqueId(boutiqueId);
        BigDecimal totalTransferred = fundTransferRepository.sumTransferredAmountByBoutiqueId(boutiqueId);
        BigDecimal availableCash = totalCashSales.subtract(totalTransferred);
        if (availableCash.compareTo(BigDecimal.ZERO) < 0) availableCash = BigDecimal.ZERO;

        log.info("getBoutiqueWalletSummary for boutiqueId={}: totalCession={}, totalPaid={}, balanceDue={}, availableCash={}", boutiqueId, totalCession, totalPaid, balanceDue, availableCash);

        return BoutiqueWalletDTO.builder()
                .boutiqueId(boutiqueId)
                .boutiqueName(boutique.getName())
                .totalCessionInvoicesAmount(totalCession)
                .totalPaidAmount(totalPaid)
                .balanceDue(balanceDue)
                .pendingTransfersAmount(pendingTransfers)
                .availableCashBalance(availableCash)
                .build();
    }

    private FundTransferDTO mapToDTO(FundTransfer t) {
        Invoice inv = t.getInvoice();
        return FundTransferDTO.builder()
                .id(t.getId())
                .reference(t.getReference())
                .boutiqueId(t.getBoutique().getId())
                .boutiqueName(t.getBoutique().getName())
                .cashSessionId(t.getCashSession() != null ? t.getCashSession().getId() : null)
                .versemenType(t.getVersemenType() != null ? t.getVersemenType() : VersementType.VERSEMENT_RECETTE)
                .invoiceId(inv != null ? inv.getId() : null)
                .invoiceNumber(inv != null ? inv.getInvoiceNumber() : null)
                .invoiceTotalAmount(inv != null ? inv.getTotalTtc() : null)
                .invoiceRemainingAmount(inv != null ? inv.getRemainingAmount() : null)
                .amount(t.getAmount())
                .paymentMethod(t.getPaymentMethod())
                .proofUrl(t.getProofUrl())
                .userEmail(t.getUserEmail())
                .approvedByEmail(t.getApprovedByEmail())
                .status(t.getStatus())
                .notes(t.getNotes())
                .rejectionReason(t.getRejectionReason())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
