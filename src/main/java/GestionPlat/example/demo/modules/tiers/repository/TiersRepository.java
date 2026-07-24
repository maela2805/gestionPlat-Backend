package GestionPlat.example.demo.modules.tiers.repository;

import GestionPlat.example.demo.modules.tiers.model.Tiers;
import GestionPlat.example.demo.modules.tiers.model.TiersStatus;
import GestionPlat.example.demo.modules.tiers.model.TiersType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TiersRepository extends JpaRepository<Tiers, Long> {
    Optional<Tiers> findByCode(String code);
    boolean existsByCode(String code);
    List<Tiers> findByType(TiersType type);
    List<Tiers> findByStatus(TiersStatus status);
    List<Tiers> findByTypeAndStatus(TiersType type, TiersStatus status);
}
