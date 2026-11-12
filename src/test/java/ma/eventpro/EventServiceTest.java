
package ma.eventpro;

import ma.eventpro.dto.request.*;
import ma.eventpro.dto.response.EvenementResponse;
import ma.eventpro.entity.*;
import ma.eventpro.entity.enums.*;
import ma.eventpro.exception.*;
import ma.eventpro.mapper.EvenementMapper;
import ma.eventpro.repository.*;
import ma.eventpro.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EvenementRepostory evenementRepostory;

    @Mock
    private OrganisateurRepository organisateurRepository;

    @Mock
    private EvenementMapper evenementMapper;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RemboursementService remboursementService;

    @InjectMocks
    private EventService eventService;

    private Organisateur organisateur;
    private Evenement evenement;
    private EvenementResponse response;
    private Reservation reservation;

    private UUID organisateurId;

    @BeforeEach
    void setUp() {

        organisateurId = UUID.randomUUID();

        organisateur = new Organisateur();
        organisateur.setId(organisateurId);

        evenement = new Evenement();
        evenement.setId(1L);
        evenement.setOrganisateur(organisateur);
        evenement.setStatus(EventStatus.DRAFT);

        CategorieBillet categorie = new CategorieBillet();
        categorie.setName("Standard");
        categorie.setPrixUnitaire(BigDecimal.valueOf(100));
        categorie.setQuantiteTotale(50);
        categorie.setQuotaMaxParClient(5);
        categorie.setEvenement(evenement);

        evenement.setCategories(new ArrayList<>(List.of(categorie)));

        response = new EvenementResponse();

        reservation = new Reservation();
        reservation.setStatus(ReservationStatus.CONFIRMED);
    }

    @Test
    void createEvent() {

        EventCreateRequest request = mock(EventCreateRequest.class);

        Instant start = Instant.now().plus(10, ChronoUnit.DAYS);

        CategorieBilletRequest categorie = CategorieBilletRequest.builder()
                .name(" Standard ")
                .prixUnitaire(BigDecimal.valueOf(100))
                .quantiteTotale(50)
                .quotaMaxParClient(5)
                .build();

        when(request.getOrganisateurId()).thenReturn(organisateurId);
        when(request.getStartDate()).thenReturn(start);
        when(request.getEndDate()).thenReturn(start.plus(2 , ChronoUnit.DAYS));
        when(request.getSalesOpeningDate()).thenReturn(null);
        when(request.getSalesClosingDate()).thenReturn(null);
        when(request.getCategories()).thenReturn(new ArrayList<>(List.of(categorie)));

        when(organisateurRepository.findById(organisateurId))
                .thenReturn(Optional.of(organisateur));

        when(evenementMapper.toEntity(request)).thenReturn(evenement);
        when(evenementRepostory.save(evenement)).thenReturn(evenement);
        when(evenementMapper.toResponse(evenement)).thenReturn(response);

        EvenementResponse result = eventService.createEvent(request);

        assertSame(response, result);
        assertSame(organisateur, evenement.getOrganisateur());
        assertEquals("Standard", categorie.getName());

        verify(evenementRepostory).save(evenement);
        verify(evenementMapper).toResponse(evenement);
    }

    @Test
    void createEventWhenOrganisateurNotFound() {

        EventCreateRequest request = mock(EventCreateRequest.class);

        when(request.getOrganisateurId()).thenReturn(organisateurId);

        when(organisateurRepository.findById(organisateurId))
                .thenReturn(Optional.empty());

        assertThrows(OrganisateurNotFound.class,
                () -> eventService.createEvent(request));

        verify(evenementRepostory, never()).save(any());
        verifyNoInteractions(evenementMapper);
    }

    @Test
    void createEventWhenStartDateIsInPast() {

        EventCreateRequest request = mock(EventCreateRequest.class);

        when(request.getOrganisateurId()).thenReturn(organisateurId);
        when(organisateurRepository.findById(organisateurId))
                .thenReturn(Optional.of(organisateur));

        when(request.getStartDate()).thenReturn(Instant.now().minus(1 , ChronoUnit.DAYS));

        assertThrows(ValidationException.class,
                () -> eventService.createEvent(request));

        verify(evenementMapper, never()).toEntity(any());
    }

    @Test
    void createEventWhenEndDateIsBeforeStartDate() {

        EventCreateRequest request = mock(EventCreateRequest.class);

        Instant start = Instant.now().plus(10 , ChronoUnit.DAYS);

        when(request.getOrganisateurId()).thenReturn(organisateurId);
        when(organisateurRepository.findById(organisateurId))
                .thenReturn(Optional.of(organisateur));

        when(request.getStartDate()).thenReturn(start);
        when(request.getEndDate()).thenReturn(start.minus(1 , ChronoUnit.DAYS));

        assertThrows(ValidationException.class,
                () -> eventService.createEvent(request));
    }

    @Test
    void createEventWhenSalesClosingBeforeOpening() {

        EventCreateRequest request = mock(EventCreateRequest.class);

        Instant start = Instant.now().plus(10 , ChronoUnit.DAYS);

        when(request.getOrganisateurId()).thenReturn(organisateurId);
        when(organisateurRepository.findById(organisateurId))
                .thenReturn(Optional.of(organisateur));

        when(request.getStartDate()).thenReturn(start);
        when(request.getEndDate()).thenReturn(start.plus(2 , ChronoUnit.DAYS));
        when(request.getSalesOpeningDate()).thenReturn(start.minus(5 , ChronoUnit.DAYS));
        when(request.getSalesClosingDate()).thenReturn(start.minus(6 , ChronoUnit.DAYS));

        assertThrows(ValidationException.class,
                () -> eventService.createEvent(request));
    }

    @Test
    void createEventWhenSalesClosingAfterEventStart() {

        EventCreateRequest request = mock(EventCreateRequest.class);

        Instant start = Instant.now().plus(10 , ChronoUnit.DAYS);

        when(request.getOrganisateurId()).thenReturn(organisateurId);
        when(organisateurRepository.findById(organisateurId))
                .thenReturn(Optional.of(organisateur));

        when(request.getStartDate()).thenReturn(start);
        when(request.getEndDate()).thenReturn(start.plus(2 , ChronoUnit.DAYS));
        when(request.getSalesOpeningDate()).thenReturn(start.minus(5 ,ChronoUnit.DAYS));
        when(request.getSalesClosingDate()).thenReturn(start.plus(1 , ChronoUnit.DAYS));

        assertThrows(ValidationException.class,
                () -> eventService.createEvent(request));
    }

    @Test
    void createEventWhenCategoriesHaveDuplicateNames() {

        EventCreateRequest request = mock(EventCreateRequest.class);

        Instant start = Instant.now().plus(10 , ChronoUnit.DAYS);

        CategorieBilletRequest first = CategorieBilletRequest.builder()
                .name("Standard")
                .build();

        CategorieBilletRequest second = CategorieBilletRequest.builder()
                .name(" STANDARD ")
                .build();

        when(request.getOrganisateurId()).thenReturn(organisateurId);
        when(organisateurRepository.findById(organisateurId))
                .thenReturn(Optional.of(organisateur));

        when(request.getStartDate()).thenReturn(start);
        when(request.getEndDate()).thenReturn(start.plus(2 , ChronoUnit.DAYS));
        when(request.getSalesOpeningDate()).thenReturn(null);
        when(request.getSalesClosingDate()).thenReturn(null);
        when(request.getCategories()).thenReturn(List.of(first, second));

        assertThrows(ConflictException.class,
                () -> eventService.createEvent(request));

        verify(evenementMapper, never()).toEntity(any());
    }


    @Test
    void publishEvent() {

        when(evenementRepostory.findById(1L))
                .thenReturn(Optional.of(evenement));

        when(evenementMapper.toResponse(evenement))
                .thenReturn(response);

        EvenementResponse result = eventService.publishEvent(1L);

        assertSame(response, result);
        assertEquals(EventStatus.PUBLISHED, evenement.getStatus());

        verify(evenementMapper).toResponse(evenement);
    }

    @Test
    void publishEventWhenEventNotFound() {

        when(evenementRepostory.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(EventNotFound.class,
                () -> eventService.publishEvent(99L));

        verifyNoInteractions(evenementMapper);
    }

    @Test
    void publishEventWhenStatusIsNotDraft() {

        evenement.setStatus(EventStatus.PUBLISHED);

        when(evenementRepostory.findById(1L))
                .thenReturn(Optional.of(evenement));

        assertThrows(ConflictException.class,
                () -> eventService.publishEvent(1L));

        verify(evenementMapper, never()).toResponse(any());
    }

    @Test
    void publishEventWhenCategoriesAreEmpty() {

        evenement.setCategories(new ArrayList<>());

        when(evenementRepostory.findById(1L))
                .thenReturn(Optional.of(evenement));

        assertThrows(ConflictException.class,
                () -> eventService.publishEvent(1L));
    }


    @Test
    void cancelEventSuccessfully() {

        when(evenementRepostory.findById(1L))
                .thenReturn(Optional.of(evenement));

        when(evenementMapper.toResponse(evenement))
                .thenReturn(response);

        EvenementResponse result = eventService.cancelEvent(1L);

        assertSame(response, result);
        assertEquals(EventStatus.CANCELED, evenement.getStatus());
    }

    @Test
    void cancelEventWhenEventNotFound() {

        when(evenementRepostory.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(EventNotFound.class,
                () -> eventService.cancelEvent(99L));
    }

    @Test
    void cancelEventWhenAlreadyCanceled() {

        evenement.setStatus(EventStatus.CANCELED);

        when(evenementRepostory.findById(1L))
                .thenReturn(Optional.of(evenement));

        assertThrows(ConflictException.class,
                () -> eventService.cancelEvent(1L));
    }

    @Test
    void cancelEventWhenCompleted() {

        evenement.setStatus(EventStatus.COMPLETED);

        when(evenementRepostory.findById(1L))
                .thenReturn(Optional.of(evenement));

        assertThrows(ConflictException.class,
                () -> eventService.cancelEvent(1L));
    }


    @Test
    void getPublishedEvents() {

        when(evenementRepostory.findByStatus(EventStatus.PUBLISHED))
                .thenReturn(List.of(evenement));

        when(evenementMapper.toResponse(evenement))
                .thenReturn(response);

        List<EvenementResponse> result = eventService.getPublishedEvents();

        assertEquals(1, result.size());
        assertSame(response, result.get(0));
    }

    @Test
    void getPublishedEventsWhenEmpty() {

        when(evenementRepostory.findByStatus(EventStatus.PUBLISHED))
                .thenReturn(Collections.emptyList());

        List<EvenementResponse> result = eventService.getPublishedEvents();

        assertTrue(result.isEmpty());
        verifyNoInteractions(evenementMapper);
    }


    @Test
    void getById() {

        when(evenementRepostory.findById(1L))
                .thenReturn(Optional.of(evenement));

        when(evenementMapper.toResponse(evenement))
                .thenReturn(response);

        assertSame(response, eventService.getById(1L));
    }

    @Test
    void getByIdWhenEventNotFound() {

        when(evenementRepostory.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(EventNotFound.class,
                () -> eventService.getById(99L));

        verifyNoInteractions(evenementMapper);
    }


    @Test
    void annulerEventSuccessfully() {

        when(evenementRepostory.findById(1L))
                .thenReturn(Optional.of(evenement));

        when(reservationRepository.findByEventIdAndStatus(
                1L, ReservationStatus.CONFIRMED))
                .thenReturn(List.of(reservation));

        eventService.annulerEvent(1L, organisateurId);

        assertEquals(EventStatus.CANCELED, evenement.getStatus());

        verify(remboursementService).returnPlaces(reservation);
    }

    @Test
    void annulerEventWhenOrganisateurIsDifferent() {

        when(evenementRepostory.findById(1L))
                .thenReturn(Optional.of(evenement));

        assertThrows(ValidationException.class,
                () -> eventService.annulerEvent(1L, UUID.randomUUID()));

        verifyNoInteractions(reservationRepository, remboursementService);
    }

    @Test
    void annulerEventWhenAlreadyCanceled() {

        evenement.setStatus(EventStatus.CANCELED);

        when(evenementRepostory.findById(1L))
                .thenReturn(Optional.of(evenement));

        assertThrows(ValidationException.class,
                () -> eventService.annulerEvent(1L, organisateurId));
    }

    @Test
    void annulerEventWhenCompleted() {

        evenement.setStatus(EventStatus.COMPLETED);

        when(evenementRepostory.findById(1L))
                .thenReturn(Optional.of(evenement));

        assertThrows(ValidationException.class,
                () -> eventService.annulerEvent(1L, organisateurId));
    }

    @Test
    void annulerEventWhenEventNotFound() {

        when(evenementRepostory.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(EventNotFound.class,
                () -> eventService.annulerEvent(99L, organisateurId));
    }

    @Test
    void annulerEventWhenNoConfirmedReservations() {

        when(evenementRepostory.findById(1L))
                .thenReturn(Optional.of(evenement));

        when(reservationRepository.findByEventIdAndStatus(
                1L, ReservationStatus.CONFIRMED))
                .thenReturn(Collections.emptyList());

        eventService.annulerEvent(1L, organisateurId);

        assertEquals(EventStatus.CANCELED, evenement.getStatus());

        verifyNoInteractions(remboursementService);
    }


    @Test
    void updateEventSuccessfully() {

        EventUpdateRequest request = mock(EventUpdateRequest.class);

        when(evenementRepostory.findById(1L))
                .thenReturn(Optional.of(evenement));

        when(evenementMapper.toResponse(evenement))
                .thenReturn(response);

        EvenementResponse result =
                eventService.updateEvent(1L, organisateurId, request);

        assertSame(response, result);

        verify(evenementMapper).updateEntity(request, evenement);
    }

    @Test
    void updateEventWhenEventNotFound() {

        EventUpdateRequest request = mock(EventUpdateRequest.class);

        when(evenementRepostory.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(EventNotFound.class,
                () -> eventService.updateEvent(99L, organisateurId, request));

        verify(evenementMapper, never()).updateEntity(any(), any());
    }

    @Test
    void updateEventWhenOrganisateurIsDifferent() {

        EventUpdateRequest request = mock(EventUpdateRequest.class);

        when(evenementRepostory.findById(1L))
                .thenReturn(Optional.of(evenement));

        assertThrows(ValidationException.class,
                () -> eventService.updateEvent(1L, UUID.randomUUID(), request));

        verify(evenementMapper, never()).updateEntity(any(), any());
    }

    @Test
    void updateEventWhenStatusIsNotDraft() {

        evenement.setStatus(EventStatus.PUBLISHED);

        EventUpdateRequest request = mock(EventUpdateRequest.class);

        when(evenementRepostory.findById(1L))
                .thenReturn(Optional.of(evenement));

        assertThrows(ValidationException.class,
                () -> eventService.updateEvent(1L, organisateurId, request));

        verify(evenementMapper, never()).updateEntity(any(), any());
    }


    @Test
    void getMyEvent() {

        when(evenementRepostory.findByOrganisateurId(organisateurId))
                .thenReturn(List.of(evenement));

        when(evenementMapper.toResponse(evenement))
                .thenReturn(response);

        List<EvenementResponse> result =
                eventService.getMyEvent(organisateurId);

        assertEquals(1, result.size());
        assertSame(response, result.get(0));
    }

    @Test
    void getMyEventWhenEmpty() {

        when(evenementRepostory.findByOrganisateurId(organisateurId))
                .thenReturn(Collections.emptyList());

        List<EvenementResponse> result =
                eventService.getMyEvent(organisateurId);

        assertTrue(result.isEmpty());
        verifyNoInteractions(evenementMapper);
    }
}
