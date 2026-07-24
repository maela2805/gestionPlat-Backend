package GestionPlat.example.demo.modules.boutique.repository;

import GestionPlat.example.demo.modules.boutique.model.Boutique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BoutiqueRepository extends JpaRepository<Boutique, Long> {
    Optional<Boutique> findByCode(String code);
    boolean existsByCode(String code);
}
