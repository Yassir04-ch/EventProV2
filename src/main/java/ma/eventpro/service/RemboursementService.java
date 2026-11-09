package ma.eventpro.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import ma.eventpro.dto.request.RemboursementCreateRequest;
import ma.eventpro.dto.response.RemboursementResponse;
import ma.eventpro.entity.*;
import ma.eventpro.entity.enums.BilletStatus;
import ma.eventpro.entity.enums.PaymentStatus;
import ma.eventpro.entity.enums.RefundStatus;
import ma.eventpro.entity.enums.ReservationStatus;
import ma.eventpro.exception.ConflictException;
import ma.eventpro.exception.PaiementNotFound;
import ma.eventpro.exception.RemboursementNotFound;
import ma.eventpro.exception.ValidationException;
import ma.eventpro.mapper.RemboursementMapper;
import ma.eventpro.repository.BilletRepository;
import ma.eventpro.repository.PaiementRepository;
import ma.eventpro.repository.RemboursementRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RemboursementService {
    private final RemboursementRepository remboursementRepository;
    private final RemboursementMapper remboursementMapper;
    private final PaiementRepository paiementRepository;
    private final BilletRepository billetRepository;

    @Transactional
    public RemboursementResponse creatRemboursement(RemboursementCreateRequest request){
        UUID clientId = request.getClientId();
        Long paiementId = request.getPaiementId();
        Paiement paiement = paiementRepository.findById(paiementId)
                .orElseThrow(()->new PaiementNotFound("paiement not found"));

        validePaiement(paiement);
        Reservation reservation = paiement.getReservation();
        List<LigneReservation> lignes = reservation.getLines();
        if (lignes.isEmpty()) {
            throw new ValidationException("La réservation ne contient aucune ligne");
        }

        Evenement evenement = lignes.get(0).getCategorieBillet().getEvenement();
        valideReservation(reservation,clientId);

        valideDate(evenement);

        List<Billet> billets = billetRepository.findByLineReservationIn(lignes);
        valideBillet(billets);
        cancelBillet(billets);

        returnPlaces(reservation);
        Remboursement remboursement = new Remboursement();
        remboursement.setPayment(paiement);
        remboursement.setStatus(RefundStatus.APPROVED);
        remboursement.setAmount(paiement.getAmount());
        remboursement.setRequestDate(Instant.now());

        reservation.setStatus(ReservationStatus.REFUNDED);
        return  remboursementMapper.toResponse(remboursementRepository.save(remboursement));
    }

    public void validePaiement(Paiement paiement){
        if(paiement.getStatus() != PaymentStatus.SUCCESS){
            throw new ValidationException("Imposible remboursée une paiement  un paiement qui n'a pas été effectué avec succès");
        }
        if(remboursementRepository.existsByPaymentId(paiement.getId())){
            throw new ConflictException("Ce paiement a déja été remboursé");
        }
    }

    public void valideReservation(Reservation reservation , UUID clientId){
        if(!reservation.getClient().getId().equals(clientId)){
            throw new ValidationException("Ce paiement n'appartient pas à ce client");
        }
        if(reservation.getStatus() != ReservationStatus.CONFIRMED){
            throw new ConflictException("Seule une réservation confirmée peut être remboursée");
        }
    }

    public void valideDate(Evenement evenement){
        LocalDateTime date = LocalDateTime.now();
        long houres = ChronoUnit.HOURS.between(date,evenement.getStartDate());
        if(houres < 48){
            throw new ValidationException("événement commence dans moins de 48 heures");
        }
    }

    public void valideBillet(List<Billet> billets){
        boolean valide = billets.stream().anyMatch(e -> e.getStatus() == BilletStatus.USED);
        if(valide){
            throw new ValidationException("un billet a déjà été utilisé");
        }
    }

    @Transactional
    public void cancelBillet(List<Billet> billets){
        for (Billet billet : billets){
            billet.setStatus(BilletStatus.CANCELED);
        }
    }

    public void returnPlaces(Reservation reservation){
        List<LigneReservation> linges = reservation.getLines();
        List<LigneReservation> listSorted = linges.stream().sorted(Comparator.comparing(l -> l.getId())).toList();
        for(LigneReservation ligne : listSorted){
           CategorieBillet categorieBillet = ligne.getCategorieBillet();
           categorieBillet.setPlacesReservees(categorieBillet.getPlacesReservees() - ligne.getQuantity());
        }
        Promotion promotion = reservation.getPromotion();
        if(promotion != null){
            promotion.setUsageCount(promotion.getUsageCount() - 1);
        }
    }

    @Transactional
    public RemboursementResponse getById(Long id) {
        return remboursementMapper.toResponse(remboursementRepository.findById(id)
                .orElseThrow(() -> new RemboursementNotFound("Remboursement not found")));
    }


}
