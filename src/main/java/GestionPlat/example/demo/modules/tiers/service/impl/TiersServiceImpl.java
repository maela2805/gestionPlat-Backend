package GestionPlat.example.demo.modules.tiers.service.impl;

import GestionPlat.example.demo.modules.tiers.dto.CreateTiersRequest;
import GestionPlat.example.demo.modules.tiers.dto.TiersDTO;
import GestionPlat.example.demo.modules.tiers.dto.UpdateTiersRequest;
import GestionPlat.example.demo.modules.tiers.model.Tiers;
import GestionPlat.example.demo.modules.tiers.model.TiersStatus;
import GestionPlat.example.demo.modules.tiers.model.TiersType;
import GestionPlat.example.demo.modules.tiers.repository.TiersRepository;
import GestionPlat.example.demo.modules.tiers.service.TiersService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TiersServiceImpl implements TiersService {

    private final TiersRepository tiersRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TiersDTO> getAllTiers(TiersType type, TiersStatus status) {
        List<Tiers> list;
        if (type != null && status != null) {
            list = tiersRepository.findByTypeAndStatus(type, status);
        } else if (type != null) {
            list = tiersRepository.findByType(type);
        } else if (status != null) {
            list = tiersRepository.findByStatus(status);
        } else {
            list = tiersRepository.findAll();
        }
        return list.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TiersDTO getTiersById(Long id) {
        Tiers tiers = tiersRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tiers introuvable avec l'ID : " + id));
        return mapToDTO(tiers);
    }

    @Override
    @Transactional
    public TiersDTO createTiers(CreateTiersRequest request) {
        String code = request.getCode();
        if (code == null || code.isBlank()) {
            String prefix = switch (request.getType()) {
                case FOURNISSEUR -> "FRS-";
                case CLIENT -> "CLT-";
                case PARTENAIRE -> "PRT-";
            };
            code = prefix + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        }

        if (tiersRepository.existsByCode(code)) {
            throw new RuntimeException("Un tiers existe déjà avec le code : " + code);
        }

        Tiers tiers = Tiers.builder()
                .code(code)
                .name(request.getName())
                .type(request.getType())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .city(request.getCity())
                .taxId(request.getTaxId())
                .status(request.getStatus() != null ? request.getStatus() : TiersStatus.ACTIF)
                .note(request.getNote())
                .build();

        Tiers saved = tiersRepository.save(tiers);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public TiersDTO updateTiers(Long id, UpdateTiersRequest request) {
        Tiers tiers = tiersRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tiers introuvable avec l'ID : " + id));

        tiers.setName(request.getName());
        tiers.setType(request.getType());
        tiers.setEmail(request.getEmail());
        tiers.setPhone(request.getPhone());
        tiers.setAddress(request.getAddress());
        tiers.setCity(request.getCity());
        tiers.setTaxId(request.getTaxId());
        tiers.setStatus(request.getStatus());
        tiers.setNote(request.getNote());

        Tiers updated = tiersRepository.save(tiers);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public void deleteTiers(Long id) {
        Tiers tiers = tiersRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tiers introuvable avec l'ID : " + id));
        tiersRepository.delete(tiers);
    }

    private TiersDTO mapToDTO(Tiers tiers) {
        return TiersDTO.builder()
                .id(tiers.getId())
                .code(tiers.getCode())
                .name(tiers.getName())
                .type(tiers.getType())
                .email(tiers.getEmail())
                .phone(tiers.getPhone())
                .address(tiers.getAddress())
                .city(tiers.getCity())
                .taxId(tiers.getTaxId())
                .status(tiers.getStatus())
                .note(tiers.getNote())
                .createdAt(tiers.getCreatedAt())
                .updatedAt(tiers.getUpdatedAt())
                .build();
    }
}
