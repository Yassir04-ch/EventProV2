package ma.eventpro.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import ma.eventpro.dto.request.CategorieBilletRequest;
import ma.eventpro.dto.request.CategorieBilletUpdateRequest;
import ma.eventpro.dto.response.CategorieBilletResponse;
import ma.eventpro.entity.CategorieBillet;
import ma.eventpro.entity.Evenement;
import ma.eventpro.entity.enums.EventStatus;
import ma.eventpro.exception.CategorieBilletNotFound;
import ma.eventpro.exception.EventNotFound;
import ma.eventpro.exception.ValidationException;
import ma.eventpro.mapper.CategorieBilletMapper;
import ma.eventpro.repository.CategorieBilletRepository;
import ma.eventpro.repository.EvenementRepostory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategorieBilletService {
    private final CategorieBilletRepository categorieBilletRepository;
    private final EvenementRepostory evenementRepostory;
    private final CategorieBilletMapper categorieBilletMapper;

    @Transactional
    public CategorieBilletResponse addCategorie(Long eventId, UUID organisateurId, CategorieBilletRequest request) {
        Evenement evenement = evenementRepostory.findById(eventId)
                .orElseThrow(() -> new EventNotFound("evenemnet not found"));
        if (!evenement.getOrganisateur().getId().equals(organisateurId)) {
            throw new ValidationException("Cet événement n'appartient pas à cet organisateur");
        }
        if (evenement.getStatus() != EventStatus.DRAFT) {
            throw new ValidationException("Impossible d'ajouter une catégorie à cet événement");
        }
        CategorieBillet categorie = categorieBilletMapper.toEntity(request);
        categorie.setEvenement(evenement);
        CategorieBillet saved = categorieBilletRepository.save(categorie);

        return categorieBilletMapper.toResponse(saved);
    }

    @Transactional
    public CategorieBilletResponse updateCategorie(Long id , UUID organisateurId , CategorieBilletUpdateRequest request){
        CategorieBillet categorie = categorieBilletRepository.findById(id)
                .orElseThrow(() -> new CategorieBilletNotFound("Catégorie introuvable"));
        Evenement evenement = categorie.getEvenement();
        if (!evenement.getOrganisateur().getId().equals(organisateurId)) {
            throw new ValidationException("Cette catégorie n'appartient pas à cet organisateur");
        }
        if (evenement.getStatus() != EventStatus.DRAFT) {
            throw new ValidationException("impossible de modifier une catégorie d'un événement non brouillon");
        }
        if (request.getQuantiteTotale() != null && request.getQuantiteTotale() < categorie.getPlacesReservees()) {
            throw new ValidationException("La quantité totale ne peut pas étre inférieure aux places déjà réservées");
        }
        categorieBilletMapper.updateEntity(request,categorie);
        return categorieBilletMapper.toResponse(categorie);
    }
}
