package ma.eventpro.repository;

import ma.eventpro.entity.LigneReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LigneReservationRepository extends JpaRepository<LigneReservation, Long> {
    List<LigneReservation> findByReservationId(Long reservationId);

    List<LigneReservation> findByCategorieBilletId(Long categorieBilletId);

    @Query("""
        SELECT COALESCE(SUM(l.quantity), 0)
        FROM LigneReservation l
        WHERE l.reservation.client.id = :clientId
          AND l.categorieBillet.id = :categorieId
    """)
    Long sumQuantityByClientAndCategorie(
            @Param("clientId") UUID clientId,
            @Param("categorieId") Long categorieId
    );
}
