package ma.eventpro.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import ma.eventpro.dto.request.PaiementCreateRequest;
import ma.eventpro.dto.response.BilletResponse;
import ma.eventpro.dto.response.PaiementResponse;
import ma.eventpro.entity.*;
import ma.eventpro.entity.enums.BilletStatus;
import ma.eventpro.entity.enums.PaymentStatus;
import ma.eventpro.entity.enums.ReservationStatus;
import ma.eventpro.exception.*;
import ma.eventpro.mapper.BilletMapper;
import ma.eventpro.mapper.PaiementMapper;
import ma.eventpro.repository.BilletRepository;
import ma.eventpro.repository.CategorieBilletRepository;
import ma.eventpro.repository.PaiementRepository;
import ma.eventpro.repository.ReservationRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaiementService {
    private final PaiementRepository paiementRepository;
    private final ReservationRepository reservationRepository;
    private final BilletRepository billetRepository;
    private final CategorieBilletRepository categorieBilletRepository;
    private final PaiementMapper paiementMapper;
    private final BilletMapper billetMapper;

    @Transactional
    public PaiementResponse createPaiement(PaiementCreateRequest request){
        Reservation reservation = reservationRepository.findById(request.getReservationId())
                .orElseThrow(()->new ReservationNotFound("résérvation not found"));
        if(reservation.getStatus() == ReservationStatus.EXPIRED){
            throw new ReservationExpired("réservation a Expired La réservation a expiré créer une nouvelle réservation");
        }
        if(reservation.getStatus() !=ReservationStatus.PENDING_PAYMENT){
            throw new ConflictException("Cette réservation ne peut plus étre payée statut: " + reservation.getStatus());
        }
        BigDecimal amount = request.getAmount();
        if(amount.compareTo(reservation.getTotalAmount()) != 0){
            throw new ValidationException("montant du paiement est not validée");
        }
        Paiement paiement = new Paiement();
        paiement.setReservation(reservation);
        paiement.setAmount(amount);
        paiement.setReference("Pay-"+ UUID.randomUUID().toString());
        paiement.setPaymentDate(Instant.now());
        paiement.setStatus(PaymentStatus.SUCCESS);
        reservation.setStatus(ReservationStatus.CONFIRMED);
        List<BilletResponse> billetResponses = addBillets(reservation);
        PaiementResponse response = paiementMapper.toResponse(paiementRepository.save(paiement));
        response.setBillets(billetResponses);
        return response;
    }

    public List<BilletResponse> addBillets(Reservation reservation){
        List<BilletResponse> billetResponses = new ArrayList<>();
        List<LigneReservation> lignes = reservation.getLines();
        for(LigneReservation ligne : lignes){
            CategorieBillet categorie = categorieBilletRepository.findById(ligne.getCategorieBillet().getId())
                    .orElseThrow(()-> new CategorieNotFound("categorie billet not found"));
            for (int i = 0 ; i < ligne.getQuantity() ; i++){
                Billet billet = new Billet();
                billet.setCode("Bil-"+UUID.randomUUID().toString());
                billet.setIssueDate(Instant.now());
                billet.setScanDate(Instant.now());
                billet.setLineReservation(ligne);
                billet.setStatus(BilletStatus.VALID);
                Billet billetsave =  billetRepository.save(billet);
                BilletResponse billetResponse = billetMapper.toResponse(billetsave);
                billetResponses.add(billetResponse);
            }
        }
        return billetResponses;
    }


}
