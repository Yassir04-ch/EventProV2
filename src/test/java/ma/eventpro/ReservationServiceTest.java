
package ma.eventpro;

import ma.eventpro.dto.request.LigneReservationRequest;
import ma.eventpro.dto.request.ReservationCreateRequest;
import ma.eventpro.dto.response.ReservationResponse;
import ma.eventpro.entity.*;
import ma.eventpro.entity.enums.*;
import ma.eventpro.exception.*;
import ma.eventpro.mapper.ReservationMapper;
import ma.eventpro.repository.*;
import ma.eventpro.service.PromotionService;
import ma.eventpro.service.ReservationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private LigneReservationRepository ligneReservationRepository;

    @Mock
    private CategorieBilletRepository categorieBilletRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private PromotionService promotionService;

    @Mock
    private ReservationMapper reservationMapper;

    @InjectMocks
    private ReservationService reservationService;

    private static final UUID CLIENT_ID = UUID.randomUUID();
    private static final Long EVENT_ID = 10L;

    private Client client;
    private Evenement evenement;
    private CategorieBillet categorie;
    private Reservation reservation;
    private ReservationResponse response;

    @BeforeEach
    void setUp() {

        client = new Client();
        client.setId(CLIENT_ID);

        evenement = new Evenement();
        evenement.setId(EVENT_ID);
        evenement.setStatus(EventStatus.PUBLISHED);

        categorie = new CategorieBillet();
        categorie.setId(1L);
        categorie.setName("Standard");
        categorie.setEvenement(evenement);
        categorie.setQuantiteTotale(100);
        categorie.setPlacesReservees(0);
        categorie.setQuotaMaxParClient(5);
        categorie.setPrixUnitaire(new BigDecimal("100.00"));

        reservation = new Reservation();
        reservation.setId(1L);
        reservation.setClient(client);
        reservation.setLines(new ArrayList<>());

        response = new ReservationResponse();
    }

    private LigneReservationRequest lineRequest(Long categorieId, int quantity) {

        LigneReservationRequest line = new LigneReservationRequest();
        line.setCategorieBilletId(categorieId);
        line.setQuantity(quantity);

        return line;
    }

    private ReservationCreateRequest createRequest(
            List<LigneReservationRequest> lines,
            String promoCode) {

        ReservationCreateRequest request = new ReservationCreateRequest();

        request.setClientId(CLIENT_ID);
        request.setLinesreserv(lines);
        request.setPromoCode(promoCode);

        return request;
    }


    @Test
    void createReservationSuccessfully() {

        ReservationCreateRequest request =
                createRequest(List.of(lineRequest(1L, 2)), null);

        when(clientRepository.findById(CLIENT_ID))
                .thenReturn(Optional.of(client));

        when(categorieBilletRepository.findById(1L))
                .thenReturn(Optional.of(categorie));

        when(ligneReservationRepository
                .sumQuantityByClientAndCategorie(CLIENT_ID, 1L))
                .thenReturn(0L);

        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(reservationMapper.toResponse(any(Reservation.class)))
                .thenReturn(response);

        ReservationResponse result =
                reservationService.createReservation(request);

        assertSame(response, result);

        assertEquals(2, categorie.getPlacesReservees());

        verify(clientRepository).findById(CLIENT_ID);
        verify(categorieBilletRepository).findById(1L);
        verify(reservationRepository).save(any(Reservation.class));
        verify(reservationMapper).toResponse(any(Reservation.class));
    }

    @Test
    void createReservationWhenClientNotFound() {

        ReservationCreateRequest request =
                createRequest(List.of(lineRequest(1L, 2)), null);

        when(clientRepository.findById(CLIENT_ID))
                .thenReturn(Optional.empty());

        assertThrows(ClientNotFound.class,
                () -> reservationService.createReservation(request));

        verifyNoInteractions(categorieBilletRepository);
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservationWhenCategoryNotFound() {

        ReservationCreateRequest request =
                createRequest(List.of(lineRequest(1L, 2)), null);

        when(clientRepository.findById(CLIENT_ID))
                .thenReturn(Optional.of(client));

        when(categorieBilletRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> reservationService.createReservation(request));

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservationWhenEventIsNotPublished() {

        evenement.setStatus(EventStatus.DRAFT);

        ReservationCreateRequest request =
                createRequest(List.of(lineRequest(1L, 2)), null);

        when(clientRepository.findById(CLIENT_ID))
                .thenReturn(Optional.of(client));

        when(categorieBilletRepository.findById(1L))
                .thenReturn(Optional.of(categorie));

        assertThrows(ValidationException.class,
                () -> reservationService.createReservation(request));

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservationWhenDifferentEvents() {

        Evenement anotherEvent = new Evenement();
        anotherEvent.setId(20L);
        anotherEvent.setStatus(EventStatus.PUBLISHED);

        CategorieBillet anotherCategory = new CategorieBillet();
        anotherCategory.setId(2L);
        anotherCategory.setName("VIP");
        anotherCategory.setEvenement(anotherEvent);
        anotherCategory.setQuantiteTotale(100);
        anotherCategory.setPlacesReservees(0);
        anotherCategory.setQuotaMaxParClient(5);
        anotherCategory.setPrixUnitaire(new BigDecimal("200.00"));

        ReservationCreateRequest request = createRequest(
                List.of(lineRequest(1L, 1), lineRequest(2L, 1)),
                null
        );

        when(clientRepository.findById(CLIENT_ID))
                .thenReturn(Optional.of(client));

        when(categorieBilletRepository.findById(1L))
                .thenReturn(Optional.of(categorie));

        when(ligneReservationRepository
                .sumQuantityByClientAndCategorie(CLIENT_ID, 1L))
                .thenReturn(0L);

        when(categorieBilletRepository.findById(2L))
                .thenReturn(Optional.of(anotherCategory));

        assertThrows(ValidationException.class,
                () -> reservationService.createReservation(request));

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservationWhenInsufficientPlaces() {

        categorie.setQuantiteTotale(10);
        categorie.setPlacesReservees(9);

        ReservationCreateRequest request =
                createRequest(List.of(lineRequest(1L, 2)), null);

        when(clientRepository.findById(CLIENT_ID))
                .thenReturn(Optional.of(client));

        when(categorieBilletRepository.findById(1L))
                .thenReturn(Optional.of(categorie));

        assertThrows(ConflictException.class,
                () -> reservationService.createReservation(request));

        verify(ligneReservationRepository, never())
                .sumQuantityByClientAndCategorie(any(), any());

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservationWhenQuotaExceeded() {

        ReservationCreateRequest request =
                createRequest(List.of(lineRequest(1L, 3)), null);

        when(clientRepository.findById(CLIENT_ID))
                .thenReturn(Optional.of(client));

        when(categorieBilletRepository.findById(1L))
                .thenReturn(Optional.of(categorie));

        when(ligneReservationRepository
                .sumQuantityByClientAndCategorie(CLIENT_ID, 1L))
                .thenReturn(4L);

        assertThrows(ConflictException.class,
                () -> reservationService.createReservation(request));

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservationWithPromotion() {

        ReservationCreateRequest request =
                createRequest(List.of(lineRequest(1L, 2)), "PROMO10");

        Promotion promotion = new Promotion();
        promotion.setDiscountType(DiscountType.PERCENTAGE);
        promotion.setDiscountValue(new BigDecimal("10"));
        promotion.setUsageCount(0);

        when(clientRepository.findById(CLIENT_ID))
                .thenReturn(Optional.of(client));

        when(categorieBilletRepository.findById(1L))
                .thenReturn(Optional.of(categorie));

        when(ligneReservationRepository
                .sumQuantityByClientAndCategorie(CLIENT_ID, 1L))
                .thenReturn(0L);

        when(promotionService.validatePromotion("PROMO10", EVENT_ID))
                .thenReturn(promotion);

        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(reservationMapper.toResponse(any(Reservation.class)))
                .thenReturn(response);

        ReservationResponse result =
                reservationService.createReservation(request);

        assertSame(response, result);

        assertEquals(1, promotion.getUsageCount());

        verify(promotionService).validatePromotion("PROMO10", EVENT_ID);
    }


    @Test
    void sortAndAvoidDuplicateSuccessfully() {

        List<LigneReservationRequest> lines = new ArrayList<>(List.of(
                lineRequest(3L, 1),
                lineRequest(1L, 2),
                lineRequest(2L, 1)
        ));

        List<LigneReservationRequest> result =
                reservationService.sortAndEviteDuplicate(lines);

        assertEquals(1L, result.get(0).getCategorieBilletId());
        assertEquals(2L, result.get(1).getCategorieBilletId());
        assertEquals(3L, result.get(2).getCategorieBilletId());
    }

    @Test
    void sortAndAvoidDuplicateWhenDuplicateExists() {

        List<LigneReservationRequest> lines = List.of(
                lineRequest(1L, 2),
                lineRequest(1L, 3)
        );

        assertThrows(ValidationException.class,
                () -> reservationService.sortAndEviteDuplicate(lines));
    }


    @Test
    void validateDisponiblesSuccessfully() {

        when(ligneReservationRepository
                .sumQuantityByClientAndCategorie(CLIENT_ID, 1L))
                .thenReturn(2L);

        assertDoesNotThrow(() ->
                reservationService.validateDisponibles(client, categorie, 2));
    }

    @Test
    void validateDisponiblesWhenQuantityExceeded() {

        categorie.setPlacesReservees(95);

        assertThrows(ConflictException.class,
                () -> reservationService.validateDisponibles(client, categorie, 10));
    }

    @Test
    void validateDisponiblesWhenQuotaExceeded() {

        when(ligneReservationRepository
                .sumQuantityByClientAndCategorie(CLIENT_ID, 1L))
                .thenReturn(4L);

        assertThrows(ConflictException.class,
                () -> reservationService.validateDisponibles(client, categorie, 2));
    }


    @Test
    void createReservationLineSuccessfully() {

        reservation.setLines(new ArrayList<>());

        LigneReservationRequest request = lineRequest(1L, 2);

        LigneReservation result = reservationService.createReservationLine(
                request, categorie, reservation
        );

        assertNotNull(result);

        assertSame(reservation, result.getReservation());
        assertSame(categorie, result.getCategorieBillet());

        assertEquals(2, result.getQuantity());

        assertEquals(
                new BigDecimal("100.00"),
                result.getFixedUnitPrice()
        );

        assertEquals(
                new BigDecimal("200.00"),
                result.getSubtotal()
        );

        assertEquals(1, reservation.getLines().size());
    }


    @Test
    void calculateDiscountWhenPromotionIsNull() {

        BigDecimal result = reservationService.calculateDiscount(
                new BigDecimal("200"),
                null
        );

        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void calculateDiscountPercentage() {

        Promotion promotion = new Promotion();
        promotion.setDiscountType(DiscountType.PERCENTAGE);
        promotion.setDiscountValue(new BigDecimal("10"));

        BigDecimal result = reservationService.calculateDiscount(
                new BigDecimal("200"),
                promotion
        );

        assertEquals(new BigDecimal("20"), result);
    }

    @Test
    void calculateDiscountFixedAmount() {

        Promotion promotion = new Promotion();
        promotion.setDiscountType(DiscountType.FIXED_AMOUNT);
        promotion.setDiscountValue(new BigDecimal("30"));

        BigDecimal result = reservationService.calculateDiscount(
                new BigDecimal("200"),
                promotion
        );

        assertEquals(new BigDecimal("30"), result);
    }

    @Test
    void calculateDiscountCannotExceedSubtotal() {

        Promotion promotion = new Promotion();
        promotion.setDiscountType(DiscountType.FIXED_AMOUNT);
        promotion.setDiscountValue(new BigDecimal("300"));

        BigDecimal result = reservationService.calculateDiscount(
                new BigDecimal("200"),
                promotion
        );

        assertEquals(new BigDecimal("200"), result);
    }


    @Test
    void cancelReservationSuccessfully() {

        reservation.setLines(new ArrayList<>());

        LigneReservation line = new LigneReservation();
        line.setQuantity(2);
        line.setCategorieBillet(categorie);

        reservation.getLines().add(line);

        categorie.setPlacesReservees(2);

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        when(categorieBilletRepository.findById(1L))
                .thenReturn(Optional.of(categorie));

        when(reservationMapper.toResponse(reservation))
                .thenReturn(response);

        ReservationResponse result =
                reservationService.cancelReservation(1L);

        assertSame(response, result);

        assertEquals(ReservationStatus.CANCELED, reservation.getStatus());
        assertEquals(0, categorie.getPlacesReservees());
    }

    @Test
    void cancelReservationWhenNotFound() {

        when(reservationRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> reservationService.cancelReservation(99L));

        verifyNoInteractions(reservationMapper);
    }

    @Test
    void cancelReservationWithPromotion() {

        Promotion promotion = new Promotion();
        promotion.setUsageCount(2);

        reservation.setPromotion(promotion);
        reservation.setLines(new ArrayList<>());

        LigneReservation line = new LigneReservation();
        line.setQuantity(2);
        line.setCategorieBillet(categorie);

        reservation.getLines().add(line);

        categorie.setPlacesReservees(2);

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        when(categorieBilletRepository.findById(1L))
                .thenReturn(Optional.of(categorie));

        when(reservationMapper.toResponse(reservation))
                .thenReturn(response);

        reservationService.cancelReservation(1L);

        assertEquals(1, promotion.getUsageCount());
        assertEquals(0, categorie.getPlacesReservees());
    }

    @Test
    void cancelReservationWhenCategoryNotFound() {

        reservation.setLines(new ArrayList<>());

        LigneReservation line = new LigneReservation();
        line.setQuantity(2);
        line.setCategorieBillet(categorie);

        reservation.getLines().add(line);

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        when(categorieBilletRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(CategorieNotFound.class,
                () -> reservationService.cancelReservation(1L));
    }


    @Test
    void getByIdSuccessfully() {

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        when(reservationMapper.toResponse(reservation))
                .thenReturn(response);

        ReservationResponse result = reservationService.getById(1L);

        assertSame(response, result);
    }

    @Test
    void getByIdWhenNotFound() {

        when(reservationRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> reservationService.getById(99L));

        verifyNoInteractions(reservationMapper);
    }


    @Test
    void expireReservationsSuccessfully() {

        reservation.setLines(new ArrayList<>());

        LigneReservation line = new LigneReservation();
        line.setQuantity(2);
        line.setCategorieBillet(categorie);

        reservation.getLines().add(line);

        categorie.setPlacesReservees(2);

        when(reservationRepository
                .findByStatusAndExpirationDateLessThanEqual(
                        eq(ReservationStatus.PENDING_PAYMENT),
                        any(Instant.class)
                ))
                .thenReturn(List.of(reservation));

        when(categorieBilletRepository.findById(1L))
                .thenReturn(Optional.of(categorie));

        reservationService.expireReservations();

        assertEquals(ReservationStatus.EXPIRED, reservation.getStatus());
        assertEquals(0, categorie.getPlacesReservees());
    }

    @Test
    void expireReservationsWhenNoExpiredReservations() {

        when(reservationRepository
                .findByStatusAndExpirationDateLessThanEqual(
                        eq(ReservationStatus.PENDING_PAYMENT),
                        any(Instant.class)
                ))
                .thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> reservationService.expireReservations());

        verifyNoInteractions(categorieBilletRepository);
    }


    @Test
    void getByClientIdSuccessfully() {

        when(reservationRepository.findByClientId(CLIENT_ID))
                .thenReturn(List.of(reservation));

        when(reservationMapper.toResponse(reservation))
                .thenReturn(response);

        List<ReservationResponse> result =
                reservationService.getByClientId(CLIENT_ID);

        assertEquals(1, result.size());
        assertSame(response, result.get(0));
    }

    @Test
    void getByClientIdWhenEmpty() {

        when(reservationRepository.findByClientId(CLIENT_ID))
                .thenReturn(Collections.emptyList());

        List<ReservationResponse> result =
                reservationService.getByClientId(CLIENT_ID);

        assertTrue(result.isEmpty());

        verifyNoInteractions(reservationMapper);
    }
}
