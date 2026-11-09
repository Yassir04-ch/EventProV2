package ma.eventpro;

import ma.eventpro.dto.request.EventCreateRequest;
import ma.eventpro.dto.response.EvenementResponse;
import ma.eventpro.entity.Evenement;
import ma.eventpro.entity.Organisateur;
import ma.eventpro.entity.Reservation;
import ma.eventpro.entity.enums.EventStatus;
import ma.eventpro.entity.enums.ReservationStatus;
import ma.eventpro.exception.EventNotFound;
import ma.eventpro.exception.OrganisateurNotFound;
import ma.eventpro.mapper.EvenementMapper;
import ma.eventpro.repository.EvenementRepostory;
import ma.eventpro.repository.OrganisateurRepository;
import ma.eventpro.repository.ReservationRepository;
import ma.eventpro.service.EventService;
import ma.eventpro.service.RemboursementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {

    @Mock
    private EvenementRepostory evenementRepostory;

    @Mock
    private OrganisateurRepository organisateurRepository;

    @Mock
    private EvenementMapper evenementMapper;

    @Mock
    private RemboursementService remboursementService;

    @Mock
    private ReservationRepository reservationRepository;
    @InjectMocks
    private EventService eventService;

    private Organisateur organisateur;
    private Evenement evenement;
    private EvenementResponse response;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        UUID organisateurId = UUID.randomUUID();
        organisateur = new Organisateur();
        organisateur.setId(organisateurId);
        evenement = new Evenement();
        response = new EvenementResponse();
        reservation = new Reservation();
        evenement.setId(1L);
        reservation.setStatus(ReservationStatus.CONFIRMED);
        evenement.setOrganisateur(organisateur);
        evenement.setStatus(EventStatus.PUBLISHED);
    }

    @Test
    void createEvent() {
        EventCreateRequest request = mock(EventCreateRequest.class);

        UUID id = UUID.randomUUID();
        when(request.getOrganisateurId()).thenReturn(id);
        when(organisateurRepository.findById(id)).thenReturn(Optional.of(organisateur));
        when(evenementMapper.toEntity(request)).thenReturn(evenement);
        when(evenementRepostory.save(evenement)).thenReturn(evenement);
        when(evenementMapper.toResponse(evenement)).thenReturn(response);
        EvenementResponse result = eventService.createEvent(request);
        assertNotNull(result);
        assertSame(response, result);
        assertSame(organisateur, evenement.getOrganisateur());

        verify(organisateurRepository).findById(id);
        verify(evenementMapper).toEntity(request);
        verify(evenementRepostory).save(evenement);
        verify(evenementMapper).toResponse(evenement);
    }

    @Test
    void createEvenWhenOrganisateurNotFound() {
        EventCreateRequest request = mock(EventCreateRequest.class);
        UUID id = UUID.randomUUID();
        when(request.getOrganisateurId()).thenReturn(id);
        when(organisateurRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(OrganisateurNotFound.class, () -> eventService.createEvent(request));
        verify(organisateurRepository).findById(id);
        verifyNoInteractions(evenementMapper);
        verify(evenementRepostory, never()).save(any());
    }

    @Test
    void publishEvent() {
        when(evenementRepostory.findById(1L)).thenReturn(Optional.of(evenement));
        when(evenementMapper.toResponse(evenement)).thenReturn(response);
        EvenementResponse result = eventService.publishEvent(1L);
        assertNotNull(result);
        assertEquals(EventStatus.PUBLISHED, evenement.getStatus());
        verify(evenementRepostory).findById(1L);
        verify(evenementMapper).toResponse(evenement);
    }

    @Test
    void publishEventWhenEventNotFound() {
        when(evenementRepostory.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EventNotFound.class, () -> eventService.publishEvent(99L));
        verify(evenementRepostory).findById(99L);
        verifyNoInteractions(evenementMapper);
    }

    @Test
    void cancelEventEventSuccessfully() {
        when(evenementRepostory.findById(1L)).thenReturn(Optional.of(evenement));
        when(evenementMapper.toResponse(evenement)).thenReturn(response);
        EvenementResponse result = eventService.cancelEvent(1L);
        assertNotNull(result);
        assertEquals(EventStatus.CANCELED, evenement.getStatus());
        verify(evenementRepostory).findById(1L);
        verify(evenementMapper).toResponse(evenement);
    }

    @Test
    void cancelEventWhenEventNotFound() {
        when(evenementRepostory.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EventNotFound.class, () -> eventService.cancelEvent(99L));
        verify(evenementRepostory).findById(99L);
        verifyNoInteractions(evenementMapper);
    }

    @Test
    void getPublishedEvents() {
        when(evenementRepostory.findByStatus(EventStatus.PUBLISHED)).thenReturn(List.of(evenement));
        when(evenementMapper.toResponse(evenement)).thenReturn(response);

        List<EvenementResponse> result = eventService.getPublishedEvents();

        assertEquals(1, result.size());
        assertSame(response, result.get(0));
        verify(evenementRepostory).findByStatus(EventStatus.PUBLISHED);
        verify(evenementMapper).toResponse(evenement);
    }

    @Test
    void getById() {
        when(evenementRepostory.findById(1L)).thenReturn(Optional.of(evenement));
        when(evenementMapper.toResponse(evenement)).thenReturn(response);
        EvenementResponse result = eventService.getById(1L);
        assertNotNull(result);
        assertSame(response, result);
        verify(evenementRepostory).findById(1L);
        verify(evenementMapper).toResponse(evenement);
    }

    @Test
    void getByIdWhenEventNotFound() {
        when(evenementRepostory.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EventNotFound.class, () -> eventService.getById(99L));
        verify(evenementRepostory).findById(99L);
        verifyNoInteractions(evenementMapper);
    }

    @Test
    void annulerEvent(){
        when(evenementRepostory.findById(1L)).thenReturn(Optional.of(evenement));
        when(reservationRepository.findByEventIdAndStatus(1L , ReservationStatus.CONFIRMED))
                .thenReturn(List.of(reservation));
        eventService.annulerEvent(1L ,organisateur.getId());
        assertEquals(EventStatus.CANCELED, evenement.getStatus());
        verify(evenementRepostory).findById(1L);
        verify(reservationRepository).findByEventIdAndStatus(1L, ReservationStatus.CONFIRMED);
    }
}
