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
import GestionPlat.example.demo.modules.billing.repository.InvoiceRepository;
import GestionPlat.example.demo.modules.billing.repository.PaymentRepository;
import GestionPlat.example.demo.modules.boutique.model.Boutique;
import GestionPlat.example.demo.modules.boutique.repository.BoutiqueRepository;
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

    @Override
    @Transactional
    public FundTransferDTO createFundTransfer(CreateFundTransferRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Long targetBoutiqueId = request.getBoutiqueId();
        if (targetBoutiqueId == null) {
            targetBoutiqueId = user.getBoutique() != null ? user.getBoutique().getId() : null;
        }

        if (targetBoutiqueId == null) {
            throw new IllegalArgumentException("Veuillez spécifier la boutique concernée par le versement.");
        }

        Boutique boutique = boutiqueRepository.findById(targetBoutiqueId)
                .orElseThrow(() -> new RuntimeException("Boutique introuvable"));

        String reference = "TRF-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        FundTransfer transfer = FundTransfer.builder()
                .reference(reference)
                .boutique(boutique)
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .proofUrl(request.getProofUrl())
                .notes(request.getNotes())
                .userEmail(userEmail)
                .status(FundTransferStatus.PENDING)
                .build();

        FundTransfer saved = fundTransferRepository.save(transfer);
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

        // 1. Auto-création d'une entrée comptable en recette
        try {
            String entryCode = "ENT-TRF-" + updated.getId() + "-" + System.currentTimeMillis() % 100000;
            AccountingEntry entry = AccountingEntry.builder()
                    .entryCode(entryCode)
                    .entryDate(LocalDateTime.now())
                    .type(EntryType.RECETTE)
                    .category(AccountingCategory.REGLEMENT_FACTURE)
                    .amount(updated.getAmount())
                    .paymentMethod(updated.getPaymentMethod())
                    .description("Versement de fonds boutique " + updated.getBoutique().getName() + " (Réf: " + updated.getReference() + ")")
                    .build();
            accountingEntryRepository.save(entry);
            log.info("Écriture comptable générée pour le versement {}", updated.getReference());
        } catch (Exception e) {
            log.error("Erreur lors de la génération de l'écriture comptable pour le versement", e);
        }

        // 2. Imputation dynamique du versement sur les factures impayées de la boutique (FIFO)
        try {
            BigDecimal availableAmount = updated.getAmount();
            List<Invoice> unpaidInvoices = invoiceRepository.findUnpaidCessionInvoicesByBoutiqueId(updated.getBoutique().getId());

            int paymentIdx = 1;
            for (Invoice invoice : unpaidInvoices) {
                if (availableAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    break;
                }

                BigDecimal invoiceRemaining = invoice.getRemainingAmount();
                BigDecimal allocatedAmount = availableAmount.min(invoiceRemaining);

                BigDecimal newPaidAmount = (invoice.getPaidAmount() != null ? invoice.getPaidAmount() : BigDecimal.ZERO).add(allocatedAmount);
                BigDecimal newRemaining = invoice.getTotalTtc().subtract(newPaidAmount);
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
                        .paymentNumber("PAY-TRF-" + updated.getId() + "-" + paymentIdx++)
                        .invoice(invoice)
                        .tiers(invoice.getTiers())
                        .paymentDate(LocalDateTime.now())
                        .amount(allocatedAmount)
                        .paymentMethod(updated.getPaymentMethod())
                        .reference(updated.getReference())
                        .note("Règlement versement de fonds boutique " + updated.getBoutique().getName() + " (Réf: " + updated.getReference() + ")")
                        .build();

                paymentRepository.save(payment);
                invoiceRepository.save(invoice);

                availableAmount = availableAmount.subtract(allocatedAmount);
                log.info("Versement {} de {} FCFA alloué sur la facture {}", updated.getReference(), allocatedAmount, invoice.getInvoiceNumber());
            }
        } catch (Exception e) {
            log.error("Erreur lors de l'imputation du versement sur les factures de cession", e);
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
        Boutique boutique = boutiqueRepository.findById(boutiqueId)
                .orElseThrow(() -> new RuntimeException("Boutique introuvable"));

        BigDecimal totalCession = invoiceRepository.sumCessionInvoicesTotalTtcByBoutiqueId(boutiqueId);
        if (totalCession == null) totalCession = BigDecimal.ZERO;

        BigDecimal totalPaid = fundTransferRepository.sumAmountByBoutiqueIdAndStatus(boutiqueId, FundTransferStatus.APPROVED);
        if (totalPaid == null) totalPaid = BigDecimal.ZERO;

        BigDecimal pendingTransfers = fundTransferRepository.sumAmountByBoutiqueIdAndStatus(boutiqueId, FundTransferStatus.PENDING);
        if (pendingTransfers == null) pendingTransfers = BigDecimal.ZERO;

        BigDecimal balanceDue = invoiceRepository.sumCessionRemainingAmountByBoutiqueId(boutiqueId);
        if (balanceDue == null) balanceDue = BigDecimal.ZERO;

        log.info("getBoutiqueWalletSummary for boutiqueId={}: totalCession={}, totalPaid={}, balanceDue={}", boutiqueId, totalCession, totalPaid, balanceDue);

        return BoutiqueWalletDTO.builder()
                .boutiqueId(boutiqueId)
                .boutiqueName(boutique.getName())
                .totalCessionInvoicesAmount(totalCession)
                .totalPaidAmount(totalPaid)
                .balanceDue(balanceDue)
                .pendingTransfersAmount(pendingTransfers)
                .build();
    }

    private FundTransferDTO mapToDTO(FundTransfer t) {
        return FundTransferDTO.builder()
                .id(t.getId())
                .reference(t.getReference())
                .boutiqueId(t.getBoutique().getId())
                .boutiqueName(t.getBoutique().getName())
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
