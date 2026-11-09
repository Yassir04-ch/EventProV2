package ma.eventpro.repository;

import ma.eventpro.entity.Billet;
import ma.eventpro.entity.LigneReservation;
import ma.eventpro.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BilletRepository extends JpaRepository<Billet , Long> {
    Optional<Billet> findByCode(String code);

    List<Billet> findByLineReservationReservationId(Long reservationId);

    List<Billet> findByLineReservationIn(List<LigneReservation> lignes);
}
