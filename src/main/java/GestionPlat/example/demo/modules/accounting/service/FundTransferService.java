package GestionPlat.example.demo.modules.accounting.service;

import GestionPlat.example.demo.modules.accounting.dto.BoutiqueWalletDTO;
import GestionPlat.example.demo.modules.accounting.dto.CreateFundTransferRequest;
import GestionPlat.example.demo.modules.accounting.dto.FundTransferDTO;
import GestionPlat.example.demo.modules.accounting.dto.RejectFundTransferRequest;
import GestionPlat.example.demo.modules.accounting.model.FundTransferStatus;

import java.util.List;

public interface FundTransferService {

    FundTransferDTO createFundTransfer(CreateFundTransferRequest request, String userEmail);

    FundTransferDTO approveFundTransfer(Long id, String userEmail);

    FundTransferDTO rejectFundTransfer(Long id, RejectFundTransferRequest request, String userEmail);

    FundTransferDTO cancelFundTransfer(Long id, String userEmail);

    FundTransferDTO getFundTransferById(Long id);

    List<FundTransferDTO> getAllFundTransfers(Long boutiqueId, FundTransferStatus status);

    BoutiqueWalletDTO getBoutiqueWalletSummary(Long boutiqueId);
}
