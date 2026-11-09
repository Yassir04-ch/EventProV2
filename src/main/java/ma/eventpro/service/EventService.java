package ma.eventpro.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import ma.eventpro.dto.request.CategorieBilletRequest;
import ma.eventpro.dto.request.EventCreateRequest;
import ma.eventpro.dto.request.EventUpdateRequest;
import ma.eventpro.dto.response.EvenementResponse;
import ma.eventpro.entity.Evenement;
import ma.eventpro.entity.Organisateur;
import ma.eventpro.entity.Reservation;
import ma.eventpro.entity.enums.EventStatus;
import ma.eventpro.entity.enums.ReservationStatus;
import ma.eventpro.exception.ConflictException;
import ma.eventpro.exception.EventNotFound;
import ma.eventpro.exception.OrganisateurNotFound;
import ma.eventpro.exception.ValidationException;
import ma.eventpro.mapper.EvenementMapper;
import ma.eventpro.repository.CategorieBilletRepository;
import ma.eventpro.repository.EvenementRepostory;
import ma.eventpro.repository.OrganisateurRepository;
import ma.eventpro.repository.ReservationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EvenementRepostory evenementRepostory;
    private final OrganisateurRepository organisateurRepository;
    private final EvenementMapper evenementMapper;
    private final ReservationRepository reservationRepository;
    private final RemboursementService remboursementService;

    @Transactional
    public EvenementResponse createEvent(EventCreateRequest request){
        Organisateur organisateur = organisateurRepository.findById(request.getOrganisateurId())
                .orElseThrow(()->new OrganisateurNotFound("Organisateur not found"));

        validateDates(request);
        validateCategories(request.getCategories());

        Evenement evenement = evenementMapper.toEntity(request);
        evenement.setOrganisateur(organisateur);
        if(evenement.getCategories() != null){
            evenement.getCategories().forEach(c ->
                    c.setEvenement(evenement));
        }
        return evenementMapper.toResponse(evenementRepostory.save(evenement));
    }

    @Transactional
    public EvenementResponse publishEvent(long id){
        Evenement evenement = evenementRepostory.findById(id)
                .orElseThrow(() -> new EventNotFound("Evenement not found"));

        if (evenement.getStatus() != EventStatus.DRAFT) {
            throw new ConflictException("Seul un événement DRAFT peut étre publié");
        }
        if (evenement.getCategories().isEmpty()) {
            throw new ConflictException("Un événement doit avoir au moins une catégorie de billets");
        }

        evenement.setStatus(EventStatus.PUBLISHED);
        return evenementMapper.toResponse(evenement);
    }

    @Transactional
    public EvenementResponse cancelEvent(Long id) {
        Evenement evenement = evenementRepostory.findById(id)
                .orElseThrow(() -> new EventNotFound("Evenement not found"));
        if (evenement.getStatus() == EventStatus.CANCELED) {
            throw new ConflictException("événement est déja annulé");
        }
        if (evenement.getStatus() == EventStatus.COMPLETED) {
            throw new ConflictException("Un événement terminé ne peut pas étre annulé");
        }
        evenement.setStatus(EventStatus.CANCELED);
        return evenementMapper.toResponse(evenement);
    }


    @Transactional
    public List<EvenementResponse> getPublishedEvents() {
        return evenementRepostory.findByStatus(EventStatus.PUBLISHED).stream()
                .map(evenementMapper::toResponse)
                .toList();
    }

    @Transactional
    public EvenementResponse getById(Long id) {
        Evenement evenement = evenementRepostory.findById(id)
                .orElseThrow(() -> new EventNotFound("Evenement not found"));;
        return evenementMapper.toResponse(evenement);
    }

    private void validateDates(EventCreateRequest request) {
        LocalDateTime start = request.getStartDate();
        LocalDateTime end = request.getEndDate();

        if (!start.isAfter(LocalDateTime.now())) {
            throw new ValidationException("date de début doit étre dans le futur");
        }
        if (!end.isAfter(start)) {
            throw new ValidationException("date de fin doit étre après la date de début");
        }

        LocalDateTime opening = request.getSalesOpeningDate();
        LocalDateTime closing = request.getSalesClosingDate();

        if (opening != null && closing != null && !closing.isAfter(opening)) {
            throw new ValidationException("cloture des ventes doit étre après l'ouverture");
        }
        if (closing != null && closing.isAfter(start)) {
            throw new ValidationException("La cloture des ventes ne peut pas dépasser le début de l'événement");
        }
    }

    private void validateCategories(List<CategorieBilletRequest> categories) {
        Set<String> names = new HashSet<>();
        for (CategorieBilletRequest categorie : categories) {
            String name = categorie.getName().trim();
            String normalizedName = name.toLowerCase(Locale.ROOT);
            if (!names.add(normalizedName)) {
                throw new ConflictException("La catégorie :" + name + " existe déja dans cet événement.");
            }
            categorie.setName(name);
        }
    }

    @Transactional
    public void annulerEvent(Long id , UUID organisateurId){
        Evenement evenement = evenementRepostory.findById(id)
                .orElseThrow(()->new EventNotFound("event not found"));
        if(!evenement.getOrganisateur().getId().equals(organisateurId)){
            throw new ValidationException("Cet événement n'appartient pas à cet organisateur");
        }
        if (evenement.getStatus() == EventStatus.CANCELED) {
            throw new ValidationException("Cet événement est déjà annulé");
        }
        if (evenement.getStatus() == EventStatus.COMPLETED) {
            throw new ValidationException("Cet événement est Terminée");
        }
        List<Reservation> reservations = reservationRepository.findByEventIdAndStatus(evenement.getId() , ReservationStatus.CONFIRMED);
        for(Reservation reservation : reservations){
            remboursementService.returnPlaces(reservation);
        }
        evenement.setStatus(EventStatus.CANCELED);
    }

    @Transactional
    public EvenementResponse updateEvent(Long id,UUID organisateurId , EventUpdateRequest request){
        Evenement evenement = evenementRepostory.findById(id)
                .orElseThrow(()->new EventNotFound("evenement not found"));
        if(!evenement.getOrganisateur().getId().equals(organisateurId)){
            throw new ValidationException("Cet événement n'appartient pas à ce organisateur");
        }
        if (evenement.getStatus() != EventStatus.DRAFT) {
            throw new ValidationException("Seuls les événements en brouillon peuvent étre modifiés");
        }
        evenementMapper.updateEntity(request , evenement);
        return evenementMapper.toResponse(evenement);
    }
    @Transactional
    public List<EvenementResponse> getMyEvent(UUID organisateurId){
        return evenementRepostory.findByOrganisateurId(organisateurId).stream()
                .map(evenementMapper::toResponse).toList();
    }
}
