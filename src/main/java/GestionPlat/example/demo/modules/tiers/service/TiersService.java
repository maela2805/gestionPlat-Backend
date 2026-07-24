package GestionPlat.example.demo.modules.tiers.service;

import GestionPlat.example.demo.modules.tiers.dto.CreateTiersRequest;
import GestionPlat.example.demo.modules.tiers.dto.TiersDTO;
import GestionPlat.example.demo.modules.tiers.dto.UpdateTiersRequest;
import GestionPlat.example.demo.modules.tiers.model.TiersStatus;
import GestionPlat.example.demo.modules.tiers.model.TiersType;

import java.util.List;

public interface TiersService {
    List<TiersDTO> getAllTiers(TiersType type, TiersStatus status);
    TiersDTO getTiersById(Long id);
    TiersDTO createTiers(CreateTiersRequest request);
    TiersDTO updateTiers(Long id, UpdateTiersRequest request);
    void deleteTiers(Long id);
}
