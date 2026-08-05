package GestionPlat.example.demo.modules.stock.service.impl;

import GestionPlat.example.demo.modules.boutique.model.Boutique;
import GestionPlat.example.demo.modules.boutique.repository.BoutiqueRepository;
import GestionPlat.example.demo.modules.stock.dto.CreateStockReturnItemRequest;
import GestionPlat.example.demo.modules.stock.dto.CreateStockReturnRequest;
import GestionPlat.example.demo.modules.stock.dto.StockReturnDTO;
import GestionPlat.example.demo.modules.stock.dto.StockReturnItemDTO;
import GestionPlat.example.demo.modules.stock.model.*;
import GestionPlat.example.demo.modules.stock.repository.BoutiqueStockRepository;
import GestionPlat.example.demo.modules.stock.repository.ProductRepository;
import GestionPlat.example.demo.modules.stock.repository.StockMovementRepository;
import GestionPlat.example.demo.modules.stock.repository.StockReturnRepository;
import GestionPlat.example.demo.modules.stock.service.StockReturnService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockReturnServiceImpl implements StockReturnService {

    private final StockReturnRepository stockReturnRepository;
    private final BoutiqueRepository boutiqueRepository;
    private final ProductRepository productRepository;
    private final BoutiqueStockRepository boutiqueStockRepository;
    private final StockMovementRepository stockMovementRepository;

    @Override
    @Transactional(readOnly = true)
    public List<StockReturnDTO> getAllStockReturns(Long boutiqueId, StockReturnStatus status) {
        List<StockReturn> returns;
        if (boutiqueId != null && status != null) {
            returns = stockReturnRepository.findByBoutiqueIdAndStatus(boutiqueId, status);
        } else if (boutiqueId != null) {
            returns = stockReturnRepository.findByBoutiqueId(boutiqueId);
        } else if (status != null) {
            returns = stockReturnRepository.findByStatus(status);
        } else {
            returns = stockReturnRepository.findAll();
        }
        return returns.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public StockReturnDTO getStockReturnById(Long id) {
        StockReturn stockReturn = stockReturnRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Déclaration de retour/casse introuvable avec l'ID : " + id));
        return mapToDTO(stockReturn);
    }

    @Override
    @Transactional
    public StockReturnDTO createStockReturn(CreateStockReturnRequest request, String userEmail) {
        Boutique boutique = boutiqueRepository.findById(request.getBoutiqueId())
                .orElseThrow(() -> new RuntimeException("Boutique introuvable avec l'ID : " + request.getBoutiqueId()));

        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String reference = "RET-" + datePrefix + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        StockReturn stockReturn = StockReturn.builder()
                .reference(reference)
                .boutique(boutique)
                .type(request.getType())
                .status(StockReturnStatus.PENDING)
                .userEmail(userEmail != null ? userEmail : "SYSTEM")
                .description(request.getDescription())
                .mediaUrls(request.getMediaUrls() != null ? request.getMediaUrls() : new ArrayList<>())
                .items(new ArrayList<>())
                .build();

        for (CreateStockReturnItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new RuntimeException("Produit introuvable avec l'ID : " + itemReq.getProductId()));

            StockReturnItem item = StockReturnItem.builder()
                    .stockReturn(stockReturn)
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(itemReq.getUnitPrice() != null ? itemReq.getUnitPrice() : product.getSellPrice())
                    .note(itemReq.getNote())
                    .build();

            stockReturn.getItems().add(item);
        }

        StockReturn saved = stockReturnRepository.save(stockReturn);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public StockReturnDTO approveStockReturn(Long id, String userEmail) {
        StockReturn stockReturn = stockReturnRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Déclaration introuvable avec l'ID : " + id));

        if (stockReturn.getStatus() != StockReturnStatus.PENDING) {
            throw new RuntimeException("Seule une déclaration 'EN ATTENTE' (PENDING) peut être approuvée.");
        }

        // Deduct items from boutique stock & record stock movements
        for (StockReturnItem item : stockReturn.getItems()) {
            Product product = item.getProduct();
            Long boutiqueId = stockReturn.getBoutique().getId();

            Optional<BoutiqueStock> bsOpt = boutiqueStockRepository.findByBoutiqueIdAndProductId(boutiqueId, product.getId());
            if (bsOpt.isPresent()) {
                BoutiqueStock bs = bsOpt.get();
                int currentQty = bs.getQuantity() != null ? bs.getQuantity() : 0;
                bs.setQuantity(Math.max(0, currentQty - item.getQuantity()));
                boutiqueStockRepository.save(bs);
            }

            // Create stock movement for trace
            StockMovement movement = StockMovement.builder()
                    .product(product)
                    .boutique(stockReturn.getBoutique())
                    .quantity(item.getQuantity())
                    .type(StockMovement.MovementType.SORTIE)
                    .reason(StockMovement.MovementReason.PERTE)
                    .userEmail(userEmail)
                    .note("Casse / Déclaration approuvée réf: " + stockReturn.getReference() + " (" + stockReturn.getType() + ")")
                    .build();

            stockMovementRepository.save(movement);
        }

        stockReturn.setStatus(StockReturnStatus.APPROVED);
        stockReturn.setApprovedByEmail(userEmail);

        return mapToDTO(stockReturnRepository.save(stockReturn));
    }

    @Override
    @Transactional
    public StockReturnDTO rejectStockReturn(Long id, String reason, String userEmail) {
        StockReturn stockReturn = stockReturnRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Déclaration introuvable avec l'ID : " + id));

        if (stockReturn.getStatus() != StockReturnStatus.PENDING) {
            throw new RuntimeException("Seule une déclaration 'EN ATTENTE' (PENDING) peut être rejetée.");
        }

        stockReturn.setStatus(StockReturnStatus.REJECTED);
        stockReturn.setRejectionReason(reason);
        stockReturn.setApprovedByEmail(userEmail);

        return mapToDTO(stockReturnRepository.save(stockReturn));
    }

    @Override
    @Transactional
    public StockReturnDTO cancelStockReturn(Long id, String userEmail) {
        StockReturn stockReturn = stockReturnRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Déclaration introuvable avec l'ID : " + id));

        if (stockReturn.getStatus() != StockReturnStatus.PENDING) {
            throw new RuntimeException("Seule une déclaration 'EN ATTENTE' (PENDING) peut être annulée.");
        }

        stockReturn.setStatus(StockReturnStatus.CANCELLED);
        return mapToDTO(stockReturnRepository.save(stockReturn));
    }

    private StockReturnDTO mapToDTO(StockReturn stockReturn) {
        List<StockReturnItemDTO> itemDTOs = stockReturn.getItems().stream().map(item ->
                StockReturnItemDTO.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productReference(item.getProduct().getReference())
                        .productName(item.getProduct().getName())
                        .productImageUrl(item.getProduct().getImageUrl())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .note(item.getNote())
                        .build()
        ).collect(Collectors.toList());

        return StockReturnDTO.builder()
                .id(stockReturn.getId())
                .reference(stockReturn.getReference())
                .boutiqueId(stockReturn.getBoutique().getId())
                .boutiqueCode(stockReturn.getBoutique().getCode())
                .boutiqueName(stockReturn.getBoutique().getName())
                .type(stockReturn.getType())
                .status(stockReturn.getStatus())
                .userEmail(stockReturn.getUserEmail())
                .approvedByEmail(stockReturn.getApprovedByEmail())
                .description(stockReturn.getDescription())
                .rejectionReason(stockReturn.getRejectionReason())
                .mediaUrls(stockReturn.getMediaUrls())
                .items(itemDTOs)
                .createdAt(stockReturn.getCreatedAt())
                .updatedAt(stockReturn.getUpdatedAt())
                .build();
    }
}
