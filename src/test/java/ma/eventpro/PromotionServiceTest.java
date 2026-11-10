package ma.eventpro;

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
import ma.eventpro.repository.PromotionRepository;
import ma.eventpro.service.PromotionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PromotionServiceTest {

    @Mock
    PromotionRepository promotionRepository;

    @Mock
    PromotionMapper promotionMapper;

    @Mock
    EvenementRepostory evenementRepostory;

    @InjectMocks
    PromotionService promotionService;

    private PromotionCreateRequest request(String code, Long evenementId, LocalDateTime start, LocalDateTime end) {
        return PromotionCreateRequest.builder()
                .evenementId(evenementId)
                .code(code)
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("10"))
                .startDate(start)
                .endDate(end)
                .usageLimit(10)
                .build();
    }

    private PromotionCreateRequest validRequest(String code, Long evenementId) {
        return request(code, evenementId, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(5));
    }

    private Promotion promotion(LocalDateTime start, LocalDateTime end, int usageCount, int usageLimit) {
        Promotion promotion = new Promotion();
        promotion.setCode("TEST10");
        promotion.setStartDate(start);
        promotion.setEndDate(end);
        promotion.setUsageCount(usageCount);
        promotion.setUsageLimit(usageLimit);
        return promotion;
    }

    private Promotion activePromotion() {
        return promotion(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(5), 0, 10);
    }

    @Test
    void createPromotionAvecEvenement() {
        PromotionCreateRequest req = validRequest("TEST10", 1L);
        Promotion promo = new Promotion();
        Evenement evenement = new Evenement();
        PromotionResponse response = new PromotionResponse();

        when(promotionRepository.existsByCode("TEST10")).thenReturn(false);
        when(promotionMapper.toEntity(req)).thenReturn(promo);
        when(evenementRepostory.findById(1L)).thenReturn(Optional.of(evenement));
        when(promotionRepository.save(promo)).thenReturn(promo);
        when(promotionMapper.toResponse(promo)).thenReturn(response);

        assertSame(response, promotionService.createPromotion(req));
        assertSame(evenement, promo.getEvenement());
    }

    @Test
    void createPromotionConflictException() {
        PromotionCreateRequest req = validRequest("TEST10", null);
        when(promotionRepository.existsByCode("TEST10")).thenReturn(true);

        assertThrows(ConflictException.class, () -> promotionService.createPromotion(req));

        verify(promotionRepository, never()).save(any());
    }

    @Test
    void createPromotionEventNotFound() {
        PromotionCreateRequest req = validRequest("TEST10", 99L);

        when(promotionRepository.existsByCode("TEST10")).thenReturn(false);
        when(promotionMapper.toEntity(req)).thenReturn(new Promotion());
        when(evenementRepostory.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EventNotFound.class, () -> promotionService.createPromotion(req));

        verify(promotionRepository, never()).save(any());
    }


    @Test
    void getByCode() {
        Promotion promo = activePromotion();
        PromotionResponse response = new PromotionResponse();

        when(promotionRepository.findByCode("TEST10")).thenReturn(Optional.of(promo));
        when(promotionMapper.toResponse(promo)).thenReturn(response);

        assertSame(response, promotionService.getByCode("TEST10"));
    }

    @Test
    void getByCodePromotionNotFound() {
        when(promotionRepository.findByCode("XXX")).thenReturn(Optional.empty());

        assertThrows(PromotionNotFound.class, () -> promotionService.getByCode("XXX"));
    }

    @Test
    void validatePromotion() {
        Promotion promo = activePromotion();
        when(promotionRepository.findByCode("TEST10")).thenReturn(Optional.of(promo));
        assertSame(promo, promotionService.validatePromotion("TEST10", 5L));
    }

    @Test
    void validatePromotion_autreEvenement_validationException() {
        Promotion promo = activePromotion();
        Evenement evenement = new Evenement();
        evenement.setId(5L);
        promo.setEvenement(evenement);
        when(promotionRepository.findByCode("TEST10")).thenReturn(Optional.of(promo));

        assertThrows(ValidationException.class, () -> promotionService.validatePromotion("TEST10", 6L));
    }

    @Test
    void validatePromotionValidationException() {
        Promotion promo = promotion(LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(1), 0, 10);
        when(promotionRepository.findByCode("TEST10")).thenReturn(Optional.of(promo));
        assertThrows(ValidationException.class, () -> promotionService.validatePromotion("TEST10", 1L));
    }

    @Test
    void validatePromotion_limiteAtteinte_validationException() {
        Promotion promo = promotion(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(5), 10, 10);
        when(promotionRepository.findByCode("TEST10")).thenReturn(Optional.of(promo));

        assertThrows(ValidationException.class, () -> promotionService.validatePromotion("TEST10", 1L));
    }

    @Test
    void validatePromotionPromotionNotFound() {
        when(promotionRepository.findByCode("TEST22")).thenReturn(Optional.empty());
        assertThrows(PromotionNotFound.class, () -> promotionService.validatePromotion("TEST22", 1L));
    }
}
