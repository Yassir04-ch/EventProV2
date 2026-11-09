package ma.eventpro.repository;

import ma.eventpro.entity.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaiementRepository extends JpaRepository<Paiement , Long> {
    boolean existsByReference(String reference);

    List<Paiement> findByReservationId(Long reservationId);
}
