package GestionPlat.example.demo.modules.boutique.service;

import GestionPlat.example.demo.modules.boutique.dto.BoutiqueDTO;
import GestionPlat.example.demo.modules.boutique.dto.BoutiquePriceDTO;
import GestionPlat.example.demo.modules.boutique.dto.CreateBoutiqueRequest;
import GestionPlat.example.demo.modules.boutique.dto.SetWholesalePriceRequest;

import java.util.List;

public interface BoutiqueService {
    List<BoutiqueDTO> getAllBoutiques();
    BoutiqueDTO getBoutiqueById(Long id);
    BoutiqueDTO createBoutique(CreateBoutiqueRequest request);
    BoutiqueDTO updateBoutique(Long id, CreateBoutiqueRequest request);
    void deleteBoutique(Long id);

    List<BoutiquePriceDTO> getBoutiquePrices(Long boutiqueId);
    BoutiquePriceDTO setWholesalePrice(Long boutiqueId, SetWholesalePriceRequest request);
}
