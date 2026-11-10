
package ma.eventpro;

import ma.eventpro.dto.request.RemboursementCreateRequest;
import ma.eventpro.dto.response.RemboursementResponse;
import ma.eventpro.entity.*;
import ma.eventpro.entity.enums.*;
import ma.eventpro.exception.*;
import ma.eventpro.mapper.RemboursementMapper;
import ma.eventpro.repository.*;
import ma.eventpro.service.RemboursementService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RemboursementServiceTest {

    @Mock
    private RemboursementRepository remboursementRepository;

    @Mock
    private RemboursementMapper remboursementMapper;

    @Mock
    private PaiementRepository paiementRepository;

    @Mock
    private BilletRepository billetRepository;

    @InjectMocks
    private RemboursementService remboursementService;

    private Paiement paiement;
    private Reservation reservation;
    private RemboursementResponse response;
    private Client client;
    private LigneReservation ligne;
    private CategorieBillet categorie;
    private Evenement evenement;
    private Billet billet;
    private UUID clientId;

    @BeforeEach
    void setUp() {

        clientId = UUID.randomUUID();

        client = new Client();
        client.setId(clientId);

        evenement = new Evenement();
        evenement.setId(10L);
        evenement.setStartDate(LocalDateTime.now().plusDays(4));

        categorie = new CategorieBillet();
        categorie.setId(1L);
        categorie.setName("Standard");
        categorie.setEvenement(evenement);
        categorie.setPlacesReservees(5);

        ligne = new LigneReservation();
        ligne.setId(1L);
        ligne.setCategorieBillet(categorie);
        ligne.setQuantity(2);

        reservation = new Reservation();
        reservation.setId(1L);
        reservation.setClient(client);
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setLines(new ArrayList<>(List.of(ligne)));

        paiement = new Paiement();
        paiement.setId(1L);
        paiement.setStatus(PaymentStatus.SUCCESS);
        paiement.setAmount(new BigDecimal("200.00"));
        paiement.setReservation(reservation);

        response = new RemboursementResponse();

        billet = new Billet();
        billet.setStatus(BilletStatus.VALID);
    }

    private RemboursementCreateRequest request() {

        RemboursementCreateRequest request =
                mock(RemboursementCreateRequest.class);

        when(request.getClientId()).thenReturn(clientId);
        when(request.getPaiementId()).thenReturn(1L);

        return request;
    }


    @Test
    void createRemboursementSuccessfully() {

        RemboursementCreateRequest request = request();

        when(paiementRepository.findById(1L))
                .thenReturn(Optional.of(paiement));

        when(remboursementRepository.existsByPaymentId(1L))
                .thenReturn(false);

        when(billetRepository.findByLineReservationIn(reservation.getLines()))
                .thenReturn(List.of(billet));

        when(remboursementRepository.save(any(Remboursement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(remboursementMapper.toResponse(any(Remboursement.class)))
                .thenReturn(response);

        RemboursementResponse result =
                remboursementService.creatRemboursement(request);

        assertSame(response, result);

        assertEquals(BilletStatus.CANCELED, billet.getStatus());

        assertEquals(ReservationStatus.REFUNDED, reservation.getStatus());

        assertEquals(3, categorie.getPlacesReservees());

        verify(paiementRepository).findById(1L);
        verify(billetRepository).findByLineReservationIn(reservation.getLines());
        verify(remboursementRepository).save(any(Remboursement.class));
        verify(remboursementMapper).toResponse(any(Remboursement.class));
    }

    @Test
    void createRemboursementWhenPaiementNotFound() {

        RemboursementCreateRequest request = request();

        when(paiementRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(PaiementNotFound.class,
                () -> remboursementService.creatRemboursement(request));

        verifyNoInteractions(remboursementMapper, billetRepository);
        verify(remboursementRepository, never()).save(any());
    }

    @Test
    void createRemboursementWhenPaymentNotSuccessful() {

        paiement.setStatus(PaymentStatus.PENDING);

        when(paiementRepository.findById(1L))
                .thenReturn(Optional.of(paiement));

        assertThrows(ValidationException.class,
                () -> remboursementService.creatRemboursement(request()));

        verify(remboursementRepository, never()).save(any());
    }

    @Test
    void createRemboursementWhenPaymentAlreadyRefunded() {

        when(paiementRepository.findById(1L))
                .thenReturn(Optional.of(paiement));

        when(remboursementRepository.existsByPaymentId(1L))
                .thenReturn(true);

        assertThrows(ConflictException.class,
                () -> remboursementService.creatRemboursement(request()));

        verify(billetRepository, never()).findByLineReservationIn(any());
        verify(remboursementRepository, never()).save(any());
    }

    @Test
    void createRemboursementWhenReservationHasNoLines() {

        reservation.setLines(new ArrayList<>());

        when(paiementRepository.findById(1L))
                .thenReturn(Optional.of(paiement));

        when(remboursementRepository.existsByPaymentId(1L))
                .thenReturn(false);

        assertThrows(ValidationException.class,
                () -> remboursementService.creatRemboursement(request()));

        verify(billetRepository, never()).findByLineReservationIn(any());
        verify(remboursementRepository, never()).save(any());
    }

    @Test
    void createRemboursementWhenAnotherClient() {

        UUID anotherClientId = UUID.randomUUID();

        when(paiementRepository.findById(1L))
                .thenReturn(Optional.of(paiement));

        when(remboursementRepository.existsByPaymentId(1L))
                .thenReturn(false);

        RemboursementCreateRequest request = mock(RemboursementCreateRequest.class);

        when(request.getPaiementId()).thenReturn(1L);
        when(request.getClientId()).thenReturn(anotherClientId);

        assertThrows(ValidationException.class,
                () -> remboursementService.creatRemboursement(request));

        verify(billetRepository, never()).findByLineReservationIn(any());
    }

    @Test
    void createRemboursementWhenReservationNotConfirmed() {

        reservation.setStatus(ReservationStatus.PENDING_PAYMENT);

        when(paiementRepository.findById(1L))
                .thenReturn(Optional.of(paiement));

        when(remboursementRepository.existsByPaymentId(1L))
                .thenReturn(false);

        assertThrows(ConflictException.class,
                () -> remboursementService.creatRemboursement(request()));

        verify(billetRepository, never()).findByLineReservationIn(any());
    }

    @Test
    void createRemboursementWhenEventStartsInLessThan48Hours() {

        evenement.setStartDate(LocalDateTime.now().plusHours(24));

        when(paiementRepository.findById(1L))
                .thenReturn(Optional.of(paiement));

        when(remboursementRepository.existsByPaymentId(1L))
                .thenReturn(false);

        assertThrows(ValidationException.class,
                () -> remboursementService.creatRemboursement(request()));

        verify(billetRepository, never()).findByLineReservationIn(any());
    }

    @Test
    void createRemboursementWhenTicketAlreadyUsed() {

        billet.setStatus(BilletStatus.USED);

        when(paiementRepository.findById(1L))
                .thenReturn(Optional.of(paiement));

        when(remboursementRepository.existsByPaymentId(1L))
                .thenReturn(false);

        when(billetRepository.findByLineReservationIn(reservation.getLines()))
                .thenReturn(List.of(billet));

        assertThrows(ValidationException.class,
                () -> remboursementService.creatRemboursement(request()));

        verify(remboursementRepository, never()).save(any());

        assertEquals(5, categorie.getPlacesReservees());
        assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus());
    }


    @Test
    void validePaiementSuccessfully() {

        when(remboursementRepository.existsByPaymentId(1L))
                .thenReturn(false);

        assertDoesNotThrow(() ->
                remboursementService.validePaiement(paiement));

        verify(remboursementRepository).existsByPaymentId(1L);
    }

    @Test
    void validePaiementWhenNotSuccessful() {

        paiement.setStatus(PaymentStatus.PENDING);

        assertThrows(ValidationException.class,
                () -> remboursementService.validePaiement(paiement));

        verifyNoInteractions(remboursementRepository);
    }

    @Test
    void validePaiementWhenAlreadyRefunded() {

        when(remboursementRepository.existsByPaymentId(1L))
                .thenReturn(true);

        assertThrows(ConflictException.class,
                () -> remboursementService.validePaiement(paiement));
    }


    @Test
    void valideReservationSuccessfully() {

        assertDoesNotThrow(() ->
                remboursementService.valideReservation(reservation, clientId));
    }

    @Test
    void valideReservationWhenAnotherClient() {

        assertThrows(ValidationException.class,
                () -> remboursementService.valideReservation(
                        reservation, UUID.randomUUID()));
    }

    @Test
    void valideReservationWhenNotConfirmed() {

        reservation.setStatus(ReservationStatus.PENDING_PAYMENT);

        assertThrows(ConflictException.class,
                () -> remboursementService.valideReservation(
                        reservation, clientId));
    }


    @Test
    void valideDateSuccessfully() {

        evenement.setStartDate(LocalDateTime.now().plusDays(5));

        assertDoesNotThrow(() ->
                remboursementService.valideDate(evenement));
    }

    @Test
    void valideDateWhenLessThan48Hours() {

        evenement.setStartDate(LocalDateTime.now().plusHours(24));

        assertThrows(ValidationException.class,
                () -> remboursementService.valideDate(evenement));
    }

    @Test
    void valideDateWhenEventAlreadyStarted() {

        evenement.setStartDate(LocalDateTime.now().minusDays(1));

        assertThrows(ValidationException.class,
                () -> remboursementService.valideDate(evenement));
    }


    @Test
    void valideBilletSuccessfully() {

        assertDoesNotThrow(() ->
                remboursementService.valideBillet(List.of(billet)));
    }

    @Test
    void valideBilletWhenAlreadyUsed() {

        billet.setStatus(BilletStatus.USED);

        assertThrows(ValidationException.class,
                () -> remboursementService.valideBillet(List.of(billet)));
    }

    @Test
    void valideBilletWhenListIsEmpty() {

        assertDoesNotThrow(() ->
                remboursementService.valideBillet(Collections.emptyList()));
    }


    @Test
    void cancelBilletSuccessfully() {

        Billet secondBillet = new Billet();
        secondBillet.setStatus(BilletStatus.VALID);

        remboursementService.cancelBillet(
                List.of(billet, secondBillet));

        assertEquals(BilletStatus.CANCELED, billet.getStatus());
        assertEquals(BilletStatus.CANCELED, secondBillet.getStatus());
    }

    @Test
    void cancelBilletWhenListIsEmpty() {

        assertDoesNotThrow(() ->
                remboursementService.cancelBillet(Collections.emptyList()));
    }


    @Test
    void returnPlacesSuccessfully() {

        categorie.setPlacesReservees(5);

        remboursementService.returnPlaces(reservation);

        assertEquals(3, categorie.getPlacesReservees());
    }

    @Test
    void returnPlacesWithPromotion() {

        Promotion promotion = new Promotion();
        promotion.setUsageCount(3);

        reservation.setPromotion(promotion);
        categorie.setPlacesReservees(5);

        remboursementService.returnPlaces(reservation);

        assertEquals(3, categorie.getPlacesReservees());
        assertEquals(2, promotion.getUsageCount());
    }

    @Test
    void returnPlacesWithoutPromotion() {

        reservation.setPromotion(null);
        categorie.setPlacesReservees(5);

        assertDoesNotThrow(() ->
                remboursementService.returnPlaces(reservation));

        assertEquals(3, categorie.getPlacesReservees());
    }

    @Test
    void returnPlacesWithMultipleLines() {

        CategorieBillet secondCategorie = new CategorieBillet();
        secondCategorie.setId(2L);
        secondCategorie.setPlacesReservees(10);

        LigneReservation secondLine = new LigneReservation();
        secondLine.setId(2L);
        secondLine.setCategorieBillet(secondCategorie);
        secondLine.setQuantity(3);

        categorie.setPlacesReservees(5);

        reservation.setLines(new ArrayList<>(
                List.of(secondLine, ligne)));

        remboursementService.returnPlaces(reservation);

        assertEquals(3, categorie.getPlacesReservees());
        assertEquals(7, secondCategorie.getPlacesReservees());
    }


    @Test
    void getByIdSuccessfully() {

        Remboursement remboursement = new Remboursement();

        when(remboursementRepository.findById(1L))
                .thenReturn(Optional.of(remboursement));

        when(remboursementMapper.toResponse(remboursement))
                .thenReturn(response);

        RemboursementResponse result =
                remboursementService.getById(1L);

        assertSame(response, result);

        verify(remboursementRepository).findById(1L);
        verify(remboursementMapper).toResponse(remboursement);
    }

    @Test
    void getByIdWhenNotFound() {

        when(remboursementRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(RemboursementNotFound.class,
                () -> remboursementService.getById(99L));

        verifyNoInteractions(remboursementMapper);
    }
}
