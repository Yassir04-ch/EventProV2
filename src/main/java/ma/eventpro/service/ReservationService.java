package ma.eventpro.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import ma.eventpro.dto.request.LigneReservationRequest;
import ma.eventpro.dto.request.ReservationCreateRequest;
import ma.eventpro.dto.response.ReservationResponse;
import ma.eventpro.entity.*;
import ma.eventpro.entity.enums.DiscountType;
import ma.eventpro.entity.enums.EventStatus;
import ma.eventpro.entity.enums.ReservationStatus;
import ma.eventpro.exception.*;
import ma.eventpro.mapper.ReservationMapper;
import ma.eventpro.repository.CategorieBilletRepository;
import ma.eventpro.repository.ClientRepository;
import ma.eventpro.repository.LigneReservationRepository;
import ma.eventpro.repository.ReservationRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private static final long HOLD_MINUTES = 10;
    private final ReservationRepository reservationRepository;
    private final LigneReservationRepository ligneReservationRepository;
    private final CategorieBilletRepository categorieBilletRepository;
    private final ClientRepository clientRepository;
    private final PromotionService promotionService;
    private final ReservationMapper reservationMapper;


    @Transactional
    public ReservationResponse createReservation(ReservationCreateRequest request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(()->new ClientNotFound("Client Not found"));

        List<LigneReservationRequest> requestedLines = sortAndEviteDuplicate(request.getLinesreserv());

        Instant now = Instant.now();
        String refecnece = "Res" + UUID.randomUUID().toString();
        Reservation reservation = new Reservation();
        reservation.setClient(client);
        reservation.setReference(refecnece);
        reservation.setCreationDate(now);
        reservation.setExpirationDate(now.plus(HOLD_MINUTES, ChronoUnit.MINUTES));
        reservation.setStatus(ReservationStatus.PENDING_PAYMENT);

        Long evenementId = null;
        BigDecimal subtotal = BigDecimal.ZERO;

        for (LigneReservationRequest lineReq : requestedLines) {

            CategorieBillet categorie = categorieBilletRepository.findById(lineReq.getCategorieBilletId())
                    .orElseThrow(() -> new ResourceNotFoundException("Catégorie de billet introuvable"));
            evenementId = validateCategoryAndEvent(categorie, evenementId);
            validateDisponibles(client, categorie, lineReq.getQuantity());
            categorie.setPlacesReservees(categorie.getPlacesReservees() + lineReq.getQuantity());
            LigneReservation line = createReservationLine(lineReq, categorie, reservation);
            subtotal = subtotal.add(line.getSubtotal());
        }
        BigDecimal discount = BigDecimal.ZERO;
        if (request.getPromoCode() != null && !request.getPromoCode().isBlank()) {
            Promotion promotion = promotionService.validatePromotion(request.getPromoCode(), evenementId);
            discount = calculateDiscount(subtotal,promotion);
            promotion.setUsageCount(promotion.getUsageCount() + 1);
            reservation.setPromotion(promotion);
        }
        reservation.setTotalAmount(subtotal.subtract(discount));
        return reservationMapper.toResponse(reservationRepository.save(reservation));
    }

    public List<LigneReservationRequest> sortAndEviteDuplicate(List<LigneReservationRequest> lignes) {
        Set<Long> seen = new HashSet<>();
        for (LigneReservationRequest ligne : lignes) {
            if (!seen.add(ligne.getCategorieBilletId())) {
                throw new ValidationException("Catégorie de billet en double : " + ligne.getCategorieBilletId());
            }
        }
        return lignes.stream().sorted(Comparator.comparing(LigneReservationRequest::getCategorieBilletId)).toList();
    }

    private Long validateCategoryAndEvent(CategorieBillet categorie, Long evenementId) {
        Evenement evenement = categorie.getEvenement();
        if (evenement.getStatus() != EventStatus.PUBLISHED) {
            throw new ValidationException("événement n'est pas ouvert à la réservation");
        }
        if (evenementId == null) {
            return evenement.getId();
        }
        if (!evenementId.equals(evenement.getId())) {
            throw new ValidationException("réservation ne peut concerner qu'un seul événement");
        }
        return evenementId;
    }

    public void validateDisponibles(Client client, CategorieBillet categorie, int quantity){
        int avaibility = categorie.getQuantiteTotale() - categorie.getPlacesReservees();
        if(quantity > avaibility){
            throw new  ConflictException("Quantity invalide dans categorie " + categorie.getName() + "places disponible est" + avaibility);
        }
        Long total = ligneReservationRepository.sumQuantityByClientAndCategorie(client.getId(),categorie.getId());
        if(total + quantity > categorie.getQuotaMaxParClient()){
            throw new ConflictException("Quota maximum dépassé pour la catégorie: " + categorie.getName());
        }
    }

    public LigneReservation createReservationLine(LigneReservationRequest ligne,CategorieBillet  categorie,Reservation  reservation){
        LigneReservation ligneReservation = new LigneReservation();
        ligneReservation.setReservation(reservation);
        ligneReservation.setCategorieBillet(categorie);
        ligneReservation.setFixedUnitPrice(categorie.getPrixUnitaire());
        ligneReservation.setQuantity(ligne.getQuantity());
        BigDecimal subTotal = categorie.getPrixUnitaire().multiply(BigDecimal.valueOf(ligneReservation.getQuantity()));
        ligneReservation.setSubtotal(subTotal);
        reservation.getLines().add(ligneReservation);
        return ligneReservation;
    }

    public BigDecimal calculateDiscount(BigDecimal subtotal, Promotion promotion){
        if(promotion == null){
            return BigDecimal.ZERO;
        }
        BigDecimal discount;
        if(promotion.getDiscountType() == DiscountType.PERCENTAGE){
            discount = subtotal.multiply(promotion.getDiscountValue()).divide(BigDecimal.valueOf(100));
        }else {
            discount = promotion.getDiscountValue();
        }

        if(discount.compareTo(subtotal) > 0){
            discount = subtotal;
        }
        return discount;
    }

    @Transactional
    public ReservationResponse cancelReservation(Long id){
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));
        returnReservedPlaces(reservation);
        reservation.setStatus(ReservationStatus.CANCELED);
        return reservationMapper.toResponse(reservation);
    }

    private void returnReservedPlaces(Reservation reservation){
        for(LigneReservation ligne : reservation.getLines()){
            Long categorieId = ligne.getCategorieBillet().getId();

            CategorieBillet categorieBillet = categorieBilletRepository.findById(categorieId)
                    .orElseThrow(() -> new CategorieNotFound("CategorieBillet Not Found"));
            categorieBillet.setPlacesReservees(categorieBillet.getPlacesReservees() - ligne.getQuantity());
        }
        Promotion promotion = reservation.getPromotion();
        if(promotion != null){
            promotion.setUsageCount(promotion.getUsageCount() - 1);
        }
    }

    @Transactional
    public ReservationResponse getById(Long id){
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation Not Found"));
        return reservationMapper.toResponse(reservation);
    }

    @Transactional
    public void expireReservations(){
        Instant now = Instant.now();
        List<Reservation> listReservation = reservationRepository.findByStatusAndExpirationDateLessThanEqual(ReservationStatus.PENDING_PAYMENT , now);
        for(Reservation reservation : listReservation){
            reservation.setStatus(ReservationStatus.EXPIRED);
            returnReservedPlaces(reservation);
        }
    }

    @Transactional
    public List<ReservationResponse> getByClientId(UUID clientId){
        return reservationRepository.findByClientId(clientId).stream()
                .map(reservationMapper::toResponse).toList();
    }
}
