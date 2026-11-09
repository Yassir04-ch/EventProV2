package ma.eventpro.repository;

import ma.eventpro.entity.Reservation;
import ma.eventpro.entity.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation , Long> {
    Optional<Reservation> findByReference(String reference);

    List<Reservation> findByClientId(UUID clientId);

    List<Reservation> findByStatusAndExpirationDateLessThanEqual(ReservationStatus status, Instant now);

    @Query("SELECT distinct r FROM Reservation r " +
            "JOIN r.lines l JOIN l.categorieBillet c WHERE c.evenement.id = :eventId " +
            "AND r.status = :status"
    )
    List<Reservation> findByEventIdAndStatus(@Param("eventId") Long eventId , @Param("status") ReservationStatus status);

}
