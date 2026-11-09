package ma.eventpro.repository;

import ma.eventpro.entity.Remboursement;
import ma.eventpro.entity.enums.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RemboursementRepository  extends JpaRepository<Remboursement , Long> {
    List<Remboursement> findByPaymentId(Long paymentId);

    List<Remboursement> findByStatus(RefundStatus status);

    boolean existsByPaymentId(Long paiementId);
}
