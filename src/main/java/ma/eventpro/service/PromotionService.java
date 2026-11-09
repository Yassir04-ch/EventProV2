package ma.eventpro.service;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.eventpro.dto.request.PromotionCreateRequest;
import ma.eventpro.dto.response.PromotionResponse;
import ma.eventpro.entity.Evenement;
import ma.eventpro.entity.Promotion;
import ma.eventpro.entity.enums.DiscountType;
import ma.eventpro.exception.ConflictException;
import ma.eventpro.exception.EventNotFound;
import ma.eventpro.exception.PromotionNotFound;
import ma.eventpro.exception.ValidationException;
import ma.eventpro.mapper.PromotionMapper;
import ma.eventpro.repository.EvenementRepostory;
import ma.eventpro.repository.OrganisateurRepository;
import ma.eventpro.repository.PromotionRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PromotionService {
    private final PromotionRepository promotionRepository;
    private final EvenementRepostory evenementRepostory;
    private final PromotionMapper promotionMapper;

    @Transactional
    public PromotionResponse createPromotion(PromotionCreateRequest request){
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new ValidationException("date de fin doit étre après la date de début");
        }
        String code = request.getCode().trim();
        if (promotionRepository.existsByCode(code)) {
            throw new ConflictException("Ce code promo existe déja : " + code);
        }
        Promotion promotion = promotionMapper.toEntity(request);
        promotion.setCode(code);

        if (request.getEvenementId() != null) {
            Evenement evenement = evenementRepostory.findById(request.getEvenementId())
                    .orElseThrow(() -> new EventNotFound("Evenement not found"));
            promotion.setEvenement(evenement);
        }
        return promotionMapper.toResponse(promotionRepository.save(promotion));
    }

    @Transactional
    public PromotionResponse getByCode(String code) {
        Promotion promotion = promotionRepository.findByCode(code).
                orElseThrow(()->new PromotionNotFound("Promotion Not Found"));
        return promotionMapper.toResponse(promotion);
    }

    @Transactional
    public Promotion validatePromotion(String code, Long evenementId) {
        Promotion promotion = promotionRepository.findByCode(code).
                orElseThrow(()->new PromotionNotFound("Promotion Not Found"));
        if (!isValid(promotion)) {
            throw new ValidationException("Code promo expiré ou limite d'utilisation atteinte");
        }
        if (promotion.getEvenement() != null && !promotion.getEvenement().getId().equals(evenementId)) {
            throw new ValidationException("Ce code promo ne s'applique pas à cet événement");
        }
        return promotion;
    }

    public boolean isValid(Promotion promotion) {
        LocalDateTime today = LocalDateTime.now();
        boolean inPeriod = !today.isBefore(promotion.getStartDate()) && !today.isAfter(promotion.getEndDate());
        boolean limitOk = promotion.getUsageCount() < promotion.getUsageLimit();
        return inPeriod && limitOk;
    }

}
