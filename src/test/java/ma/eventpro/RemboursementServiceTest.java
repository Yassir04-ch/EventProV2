package ma.eventpro;

import ma.eventpro.dto.request.RemboursementCreateRequest;
import ma.eventpro.dto.response.RemboursementResponse;
import ma.eventpro.entity.*;
import ma.eventpro.entity.enums.BilletStatus;
import ma.eventpro.entity.enums.PaymentStatus;
import ma.eventpro.entity.enums.ReservationStatus;
import ma.eventpro.exception.ConflictException;
import ma.eventpro.exception.PaiementNotFound;
import ma.eventpro.exception.ValidationException;
import ma.eventpro.mapper.RemboursementMapper;
import ma.eventpro.repository.BilletRepository;
import ma.eventpro.repository.PaiementRepository;
import ma.eventpro.repository.RemboursementRepository;
import ma.eventpro.service.RemboursementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RemboursementServiceTest {
    @Mock
    RemboursementRepository remboursementRepository;
    @Mock
    RemboursementMapper remboursementMapper;
    @Mock
    PaiementRepository paiementRepository;
    @Mock
    BilletRepository billetRepository;
    @Mock
    RemboursementCreateRequest request;

    @InjectMocks
    RemboursementService remboursementService;

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

        reservation = new Reservation();
        reservation.setClient(client);
        reservation.setStatus(ReservationStatus.CONFIRMED);

        paiement = new Paiement();
        paiement.setId(1L);
        paiement.setStatus(PaymentStatus.SUCCESS);
        paiement.setReservation(reservation);

        response = new RemboursementResponse();

        evenement = new Evenement();
        evenement.setStartDate(LocalDateTime.now().plusDays(4));

        categorie = new CategorieBillet();
        categorie.setEvenement(evenement);

        ligne = new LigneReservation();
        ligne.setCategorieBillet(categorie);
        ligne.setQuantity(2);

        reservation.setLines(List.of(ligne));

        billet = new Billet();
        billet.setStatus(BilletStatus.VALID);
    }

    @Test
    void createRemboursementWhenPaiementNotFound() {

        Long paiementId = 1L;

        when(request.getPaiementId()).thenReturn(paiementId);
        when(request.getClientId()).thenReturn(clientId);

        when(paiementRepository.findById(paiementId)).thenReturn(Optional.empty());

        assertThrows(PaiementNotFound.class,
                () -> remboursementService.creatRemboursement(request)
        );

        verify(paiementRepository).findById(paiementId);
        verify(paiementRepository, never()).save(any(Paiement.class));
    }

    @Test
    void createRemboursementWhenPaymentNotSuccessful() {

        Long paiementId = 1L;

        paiement.setStatus(PaymentStatus.PENDING);

        when(request.getPaiementId()).thenReturn(paiementId);
        when(request.getClientId()).thenReturn(clientId);
        when(paiementRepository.findById(paiementId)).thenReturn(Optional.of(paiement));

        assertThrows(
                ValidationException.class,
                () -> remboursementService.creatRemboursement(request)
        );

        verify(remboursementRepository, never()).save(any(Remboursement.class));
    }

    @Test
    void createRemboursementWhenPaymentAlreadyRefunded() {

        Long paiementId = 1L;

        when(request.getPaiementId()).thenReturn(paiementId);
        when(request.getClientId()).thenReturn(clientId);

        when(paiementRepository.findById(paiementId)).thenReturn(Optional.of(paiement));

        when(remboursementRepository.existsByPaymentId(paiementId)).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> remboursementService.creatRemboursement(request)
        );

        verify(remboursementRepository, never()).save(any(Remboursement.class));
    }

    @Test
    void createRemboursementWhenReservationBelongsToAnotherClient() {

        Long paiementId = 1L;

        when(request.getPaiementId()).thenReturn(paiementId);
        when(request.getClientId()).thenReturn(UUID.randomUUID());

        when(paiementRepository.findById(paiementId)).thenReturn(Optional.of(paiement));

        when(remboursementRepository.existsByPaymentId(paiementId)).thenReturn(false);

        assertThrows(ValidationException.class,
                () -> remboursementService.creatRemboursement(request)
        );

        verify(remboursementRepository, never()).save(any(Remboursement.class));
    }

    @Test
    void createRemboursementWhenReservationNotConfirmed() {

        Long paiementId = 1L;
        reservation.setStatus(ReservationStatus.PENDING_PAYMENT);

        when(request.getPaiementId()).thenReturn(paiementId);
        when(request.getClientId()).thenReturn(clientId);

        when(paiementRepository.findById(paiementId)).thenReturn(Optional.of(paiement));

        when(remboursementRepository.existsByPaymentId(paiementId)).thenReturn(false);

        assertThrows(ConflictException.class,
                () -> remboursementService.creatRemboursement(request)
        );

        verify(remboursementRepository, never()).save(any(Remboursement.class));
    }

    @Test
    void createRemboursementWhenEventStartsInLessThan48Hours() {

        Long paiementId = 1L;

        evenement.setStartDate(LocalDateTime.now().plusHours(24));

        when(request.getPaiementId()).thenReturn(paiementId);
        when(request.getClientId()).thenReturn(clientId);

        when(paiementRepository.findById(paiementId)).thenReturn(Optional.of(paiement));

        when(remboursementRepository.existsByPaymentId(paiementId)).thenReturn(false);

        assertThrows(ValidationException.class,
                () -> remboursementService.creatRemboursement(request)
        );

        verify(remboursementRepository, never()).save(any(Remboursement.class));
    }

    @Test
    void createRemboursementWhenTicketAlreadyUsed() {

        Long paiementId = 1L;

        billet.setStatus(BilletStatus.USED);

        when(request.getPaiementId()).thenReturn(paiementId);
        when(request.getClientId()).thenReturn(clientId);

        when(paiementRepository.findById(paiementId)).thenReturn(Optional.of(paiement));

        when(remboursementRepository.existsByPaymentId(paiementId)).thenReturn(false);

        when(billetRepository.findByLineReservationIn(reservation.getLines())).thenReturn(List.of(billet));

        assertThrows(ValidationException.class,
                () -> remboursementService.creatRemboursement(request)
);

        verify(remboursementRepository, never()).save(any(Remboursement.class));
    }

    @Test
    void validePaiement() {
        when(remboursementRepository.existsByPaymentId(1L)).thenReturn(false);
        assertDoesNotThrow(() -> remboursementService.validePaiement(paiement));
        verify(remboursementRepository).existsByPaymentId(1L);
    }

    @Test
    void valideReservation() {
        assertDoesNotThrow(() -> remboursementService.valideReservation(reservation, clientId));
    }

    @Test
    void valideDate() {
        assertDoesNotThrow(() -> remboursementService.valideDate(evenement));
    }

    @Test
    void valideBillet() {
        assertDoesNotThrow(() -> remboursementService.valideBillet(List.of(billet)));
    }

    @Test
    void cancelBillet() {
        remboursementService.cancelBillet(List.of(billet));
        assertEquals(BilletStatus.CANCELED, billet.getStatus());
    }
}
