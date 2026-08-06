package GestionPlat.example.demo.modules.caisse.service.impl;

import GestionPlat.example.demo.modules.boutique.model.Boutique;
import GestionPlat.example.demo.modules.boutique.repository.BoutiqueRepository;
import GestionPlat.example.demo.modules.caisse.dto.*;
import GestionPlat.example.demo.modules.caisse.model.CashMovement;
import GestionPlat.example.demo.modules.caisse.model.CashMovementType;
import GestionPlat.example.demo.modules.caisse.model.CashSession;
import GestionPlat.example.demo.modules.caisse.model.CashSessionStatus;
import GestionPlat.example.demo.modules.caisse.repository.CashMovementRepository;
import GestionPlat.example.demo.modules.caisse.repository.CashSessionRepository;
import GestionPlat.example.demo.modules.caisse.service.CashSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CashSessionServiceImpl implements CashSessionService {

    private final CashSessionRepository cashSessionRepository;
    private final CashMovementRepository cashMovementRepository;
    private final BoutiqueRepository boutiqueRepository;
    private final GestionPlat.example.demo.modules.accounting.repository.FundTransferRepository fundTransferRepository;

    @Override
    @Transactional
    public CashSessionDTO openSession(OpenCashSessionRequest request, String userEmail) {
        Boutique boutique = boutiqueRepository.findById(request.getBoutiqueId())
                .orElseThrow(() -> new RuntimeException("Boutique introuvable avec l'ID : " + request.getBoutiqueId()));

        if (cashSessionRepository.existsByBoutiqueIdAndStatus(boutique.getId(), CashSessionStatus.OPEN)) {
            throw new RuntimeException("Une session de caisse est déjà OUVERTE pour la boutique : " + boutique.getName());
        }

        String reference = "CAS-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        BigDecimal openingBalance = request.getOpeningBalance() != null ? request.getOpeningBalance() : BigDecimal.ZERO;

        CashSession session = CashSession.builder()
                .sessionReference(reference)
                .boutique(boutique)
                .userEmail(userEmail != null ? userEmail : "SYSTEM")
                .openingDate(LocalDateTime.now())
                .openingBalance(openingBalance)
                .closingBalanceExpected(openingBalance)
                .status(CashSessionStatus.OPEN)
                .notes(request.getNotes())
                .build();

        CashSession saved = cashSessionRepository.save(session);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public CashSessionDTO closeSession(Long sessionId, CloseCashSessionRequest request, String userEmail) {
        CashSession session = cashSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session de caisse introuvable avec l'ID : " + sessionId));

        if (session.getStatus() == CashSessionStatus.CLOSED) {
            throw new RuntimeException("Cette session de caisse est déjà FERMÉE.");
        }

        List<CashMovement> movements = cashMovementRepository.findByCashSessionIdOrderByCreatedAtDesc(sessionId);
        BigDecimal totalCashIn = movements.stream()
                .filter(m -> m.getType() == CashMovementType.ENTREE)
                .map(CashMovement::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCashOut = movements.stream()
                .filter(m -> m.getType() == CashMovementType.SORTIE)
                .map(CashMovement::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Expected Cash = Opening Balance + Total Sales Cash + Cash In Movements - Cash Out Movements
        BigDecimal expectedBalance = session.getOpeningBalance()
                .add(session.getTotalSalesCash())
                .add(totalCashIn)
                .subtract(totalCashOut);

        BigDecimal realBalance = request.getClosingBalanceReal() != null ? request.getClosingBalanceReal() : BigDecimal.ZERO;
        BigDecimal difference = realBalance.subtract(expectedBalance);

        session.setClosingDate(LocalDateTime.now());
        session.setClosingBalanceExpected(expectedBalance);
        session.setClosingBalanceReal(realBalance);
        session.setCashDifference(difference);
        session.setStatus(CashSessionStatus.CLOSED);

        if (request.getNotes() != null && !request.getNotes().isBlank()) {
            String combinedNotes = (session.getNotes() != null ? session.getNotes() + " | Clôture: " : "Clôture: ") + request.getNotes();
            session.setNotes(combinedNotes);
        }

        CashSession saved = cashSessionRepository.save(session);
        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CashSessionDTO getCurrentSessionForUser(String userEmail) {
        CashSession session = cashSessionRepository.findByUserEmailAndStatus(userEmail, CashSessionStatus.OPEN)
                .orElse(null);
        return session != null ? mapToDTO(session) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public CashSessionDTO getCurrentSessionForBoutique(Long boutiqueId) {
        CashSession session = cashSessionRepository.findByBoutiqueIdAndStatus(boutiqueId, CashSessionStatus.OPEN)
                .orElse(null);
        return session != null ? mapToDTO(session) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public CashSessionDTO getSessionById(Long id) {
        CashSession session = cashSessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session de caisse introuvable avec l'ID : " + id));
        return mapToDTO(session);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CashSessionDTO> getAllSessions() {
        return cashSessionRepository.findAllByOrderByOpeningDateDesc().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CashSessionDTO> getSessionsByBoutique(Long boutiqueId) {
        return cashSessionRepository.findByBoutiqueIdOrderByOpeningDateDesc(boutiqueId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CashMovementDTO addMovement(Long sessionId, CreateCashMovementRequest request, String userEmail) {
        CashSession session = cashSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session de caisse introuvable avec l'ID : " + sessionId));

        if (session.getStatus() == CashSessionStatus.CLOSED) {
            throw new RuntimeException("Impossible d'ajouter un mouvement sur une session de caisse FERMÉE.");
        }

        CashMovement movement = CashMovement.builder()
                .cashSession(session)
                .type(request.getType())
                .amount(request.getAmount())
                .reason(request.getReason())
                .userEmail(userEmail != null ? userEmail : "SYSTEM")
                .build();

        CashMovement saved = cashMovementRepository.save(movement);

        // Recalculate Expected Closing Cash Balance for open session preview
        recalculateSessionBalances(session);

        return mapMovementToDTO(saved);
    }

    private void recalculateSessionBalances(CashSession session) {
        List<CashMovement> movements = cashMovementRepository.findByCashSessionIdOrderByCreatedAtDesc(session.getId());
        BigDecimal totalCashIn = movements.stream()
                .filter(m -> m.getType() == CashMovementType.ENTREE)
                .map(CashMovement::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCashOut = movements.stream()
                .filter(m -> m.getType() == CashMovementType.SORTIE)
                .map(CashMovement::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal expected = session.getOpeningBalance()
                .add(session.getTotalSalesCash())
                .add(totalCashIn)
                .subtract(totalCashOut);

        session.setClosingBalanceExpected(expected);
        cashSessionRepository.save(session);
    }

    private CashSessionDTO mapToDTO(CashSession session) {
        List<CashMovement> movements = cashMovementRepository.findByCashSessionIdOrderByCreatedAtDesc(session.getId());
        List<CashMovementDTO> movementDTOs = movements.stream().map(this::mapMovementToDTO).collect(Collectors.toList());

        BigDecimal totalCashIn = movements.stream()
                .filter(m -> m.getType() == CashMovementType.ENTREE)
                .map(CashMovement::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCashOut = movements.stream()
                .filter(m -> m.getType() == CashMovementType.SORTIE)
                .map(CashMovement::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalTransferred = fundTransferRepository.sumAmountByCashSessionId(session.getId());
        if (totalTransferred == null) totalTransferred = BigDecimal.ZERO;

        BigDecimal targetAmount = session.getClosingBalanceReal() != null ? session.getClosingBalanceReal() : session.getClosingBalanceExpected();
        if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) == 0) {
            targetAmount = session.getTotalSalesCash();
        }
        BigDecimal remaining = targetAmount.subtract(totalTransferred);
        if (remaining.compareTo(BigDecimal.ZERO) < 0) remaining = BigDecimal.ZERO;

        return CashSessionDTO.builder()
                .id(session.getId())
                .sessionReference(session.getSessionReference())
                .boutiqueId(session.getBoutique().getId())
                .boutiqueCode(session.getBoutique().getCode())
                .boutiqueName(session.getBoutique().getName())
                .userEmail(session.getUserEmail())
                .openingDate(session.getOpeningDate())
                .closingDate(session.getClosingDate())
                .openingBalance(session.getOpeningBalance())
                .closingBalanceExpected(session.getClosingBalanceExpected())
                .closingBalanceReal(session.getClosingBalanceReal())
                .cashDifference(session.getCashDifference())
                .totalSalesCash(session.getTotalSalesCash())
                .totalSalesMobileMoney(session.getTotalSalesMobileMoney())
                .totalSalesCard(session.getTotalSalesCard())
                .totalSalesOther(session.getTotalSalesOther())
                .totalTransferredAmount(totalTransferred)
                .remainingToTransfer(remaining)
                .totalCashIn(totalCashIn)
                .totalCashOut(totalCashOut)
                .status(session.getStatus())
                .notes(session.getNotes())
                .movements(movementDTOs)
                .createdAt(session.getCreatedAt())
                .build();
    }

    private CashMovementDTO mapMovementToDTO(CashMovement m) {
        return CashMovementDTO.builder()
                .id(m.getId())
                .cashSessionId(m.getCashSession().getId())
                .type(m.getType())
                .amount(m.getAmount())
                .reason(m.getReason())
                .userEmail(m.getUserEmail())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
