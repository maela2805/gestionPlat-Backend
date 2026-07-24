package GestionPlat.example.demo.modules.sale.service;

import GestionPlat.example.demo.modules.sale.dto.CreateStoreSaleRequest;
import GestionPlat.example.demo.modules.sale.dto.StoreSaleDTO;

import java.util.List;

public interface StoreSaleService {
    List<StoreSaleDTO> getAllStoreSales();
    StoreSaleDTO getStoreSaleById(Long id);
    StoreSaleDTO createStoreSale(CreateStoreSaleRequest request, String userEmail);
    StoreSaleDTO validateStoreSale(Long id, String userEmail);
    StoreSaleDTO cancelStoreSale(Long id);
}
