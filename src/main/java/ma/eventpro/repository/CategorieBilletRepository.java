package ma.eventpro.repository;

import ma.eventpro.entity.CategorieBillet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategorieBilletRepository extends JpaRepository<CategorieBillet, Long> {
    List<CategorieBillet> findByEvenementId(Long evenementId);
    boolean existsByEvenementTitle(String title);
}
