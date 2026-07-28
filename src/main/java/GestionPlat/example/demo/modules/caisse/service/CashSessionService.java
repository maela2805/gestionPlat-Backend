package GestionPlat.example.demo.modules.caisse.service;

import GestionPlat.example.demo.modules.caisse.dto.*;

import java.util.List;

public interface CashSessionService {
    CashSessionDTO openSession(OpenCashSessionRequest request, String userEmail);
    CashSessionDTO closeSession(Long sessionId, CloseCashSessionRequest request, String userEmail);
    CashSessionDTO getCurrentSessionForUser(String userEmail);
    CashSessionDTO getCurrentSessionForBoutique(Long boutiqueId);
    CashSessionDTO getSessionById(Long id);
    List<CashSessionDTO> getAllSessions();
    List<CashSessionDTO> getSessionsByBoutique(Long boutiqueId);
    CashMovementDTO addMovement(Long sessionId, CreateCashMovementRequest request, String userEmail);
}
