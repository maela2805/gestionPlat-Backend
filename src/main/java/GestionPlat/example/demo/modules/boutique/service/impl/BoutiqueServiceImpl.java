package GestionPlat.example.demo.modules.boutique.service.impl;

import GestionPlat.example.demo.modules.boutique.dto.BoutiqueDTO;
import GestionPlat.example.demo.modules.boutique.dto.BoutiquePriceDTO;
import GestionPlat.example.demo.modules.boutique.dto.CreateBoutiqueRequest;
import GestionPlat.example.demo.modules.boutique.dto.SetWholesalePriceRequest;
import GestionPlat.example.demo.modules.boutique.model.Boutique;
import GestionPlat.example.demo.modules.boutique.model.BoutiqueWholesalePrice;
import GestionPlat.example.demo.modules.boutique.repository.BoutiqueRepository;
import GestionPlat.example.demo.modules.boutique.repository.BoutiqueWholesalePriceRepository;
import GestionPlat.example.demo.modules.boutique.service.BoutiqueService;
import GestionPlat.example.demo.modules.stock.model.Product;
import GestionPlat.example.demo.modules.stock.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoutiqueServiceImpl implements BoutiqueService {

    private final BoutiqueRepository boutiqueRepository;
    private final BoutiqueWholesalePriceRepository wholesalePriceRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public List<BoutiqueDTO> getAllBoutiques() {
        return boutiqueRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BoutiqueDTO getBoutiqueById(Long id) {
        Boutique boutique = boutiqueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Boutique introuvable avec l'ID : " + id));
        return mapToDTO(boutique);
    }

    @Override
    @Transactional
    public BoutiqueDTO createBoutique(CreateBoutiqueRequest request) {
        String code = request.getCode();
        if (code == null || code.isBlank()) {
            code = "BTQ-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        }

        if (boutiqueRepository.existsByCode(code)) {
            throw new RuntimeException("Une boutique existe déjà avec le code : " + code);
        }

        Boutique boutique = Boutique.builder()
                .code(code)
                .name(request.getName())
                .address(request.getAddress())
                .city(request.getCity())
                .phone(request.getPhone())
                .managerName(request.getManagerName())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        Boutique saved = boutiqueRepository.save(boutique);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public BoutiqueDTO updateBoutique(Long id, CreateBoutiqueRequest request) {
        Boutique boutique = boutiqueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Boutique introuvable avec l'ID : " + id));

        boutique.setName(request.getName());
        boutique.setAddress(request.getAddress());
        boutique.setCity(request.getCity());
        boutique.setPhone(request.getPhone());
        boutique.setManagerName(request.getManagerName());
        if (request.getActive() != null) {
            boutique.setActive(request.getActive());
        }

        Boutique updated = boutiqueRepository.save(boutique);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public void deleteBoutique(Long id) {
        Boutique boutique = boutiqueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Boutique introuvable avec l'ID : " + id));
        boutiqueRepository.delete(boutique);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoutiquePriceDTO> getBoutiquePrices(Long boutiqueId) {
        Boutique boutique = boutiqueRepository.findById(boutiqueId)
                .orElseThrow(() -> new RuntimeException("Boutique introuvable avec l'ID : " + boutiqueId));

        List<Product> products = productRepository.findAll();
        List<BoutiqueWholesalePrice> existingPrices = wholesalePriceRepository.findByBoutiqueId(boutiqueId);

        Map<Long, BoutiqueWholesalePrice> priceMap = existingPrices.stream()
                .collect(Collectors.toMap(p -> p.getProduct().getId(), Function.identity()));

        return products.stream().map(product -> {
            BoutiqueWholesalePrice price = priceMap.get(product.getId());
            return BoutiquePriceDTO.builder()
                    .id(price != null ? price.getId() : null)
                    .boutiqueId(boutique.getId())
                    .boutiqueName(boutique.getName())
                    .productId(product.getId())
                    .productReference(product.getReference())
                    .productName(product.getName())
                    .defaultBuyPrice(product.getBuyPrice())
                    .defaultSellPrice(product.getSellPrice())
                    .wholesalePrice(price != null ? price.getWholesalePrice() : product.getSellPrice())
                    .active(price != null ? price.getActive() : true)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BoutiquePriceDTO setWholesalePrice(Long boutiqueId, SetWholesalePriceRequest request) {
        Boutique boutique = boutiqueRepository.findById(boutiqueId)
                .orElseThrow(() -> new RuntimeException("Boutique introuvable avec l'ID : " + boutiqueId));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Produit introuvable avec l'ID : " + request.getProductId()));

        BoutiqueWholesalePrice price = wholesalePriceRepository
                .findByBoutiqueIdAndProductId(boutiqueId, request.getProductId())
                .orElseGet(() -> BoutiqueWholesalePrice.builder()
                        .boutique(boutique)
                        .product(product)
                        .build());

        price.setWholesalePrice(request.getWholesalePrice());
        price.setActive(request.getActive() != null ? request.getActive() : true);

        BoutiqueWholesalePrice saved = wholesalePriceRepository.save(price);

        return BoutiquePriceDTO.builder()
                .id(saved.getId())
                .boutiqueId(boutique.getId())
                .boutiqueName(boutique.getName())
                .productId(product.getId())
                .productReference(product.getReference())
                .productName(product.getName())
                .defaultBuyPrice(product.getBuyPrice())
                .defaultSellPrice(product.getSellPrice())
                .wholesalePrice(saved.getWholesalePrice())
                .active(saved.getActive())
                .build();
    }

    private BoutiqueDTO mapToDTO(Boutique boutique) {
        return BoutiqueDTO.builder()
                .id(boutique.getId())
                .code(boutique.getCode())
                .name(boutique.getName())
                .address(boutique.getAddress())
                .city(boutique.getCity())
                .phone(boutique.getPhone())
                .managerName(boutique.getManagerName())
                .active(boutique.getActive())
                .createdAt(boutique.getCreatedAt())
                .build();
    }
}
