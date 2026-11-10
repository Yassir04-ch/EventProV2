
package ma.eventpro;

import ma.eventpro.dto.request.PaiementCreateRequest;
import ma.eventpro.dto.response.BilletResponse;
import ma.eventpro.dto.response.PaiementResponse;
import ma.eventpro.entity.*;
import ma.eventpro.entity.enums.BilletStatus;
import ma.eventpro.entity.enums.PaymentStatus;
import ma.eventpro.entity.enums.ReservationStatus;
import ma.eventpro.exception.CategorieNotFound;
import ma.eventpro.exception.ConflictException;
import ma.eventpro.exception.ReservationExpired;
import ma.eventpro.exception.ReservationNotFound;
import ma.eventpro.exception.ValidationException;
import ma.eventpro.mapper.BilletMapper;
import ma.eventpro.mapper.PaiementMapper;
import ma.eventpro.repository.BilletRepository;
import ma.eventpro.repository.CategorieBilletRepository;
import ma.eventpro.repository.PaiementRepository;
import ma.eventpro.repository.ReservationRepository;
import ma.eventpro.service.PaiementService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaiementServiceTest {

    @Mock
    private PaiementRepository paiementRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private BilletRepository billetRepository;

    @Mock
    private CategorieBilletRepository categorieBilletRepository;

    @Mock
    private PaiementMapper paiementMapper;

    @Mock
    private BilletMapper billetMapper;

    @Mock
    private PaiementCreateRequest request;

    @InjectMocks
    private PaiementService paiementService;

    private Reservation reservation;
    private CategorieBillet categorie;
    private LigneReservation ligne;
    private PaiementResponse paiementResponse;
    private BilletResponse billetResponse;

    @BeforeEach
    void setUp() {

        reservation = new Reservation();
        reservation.setTotalAmount(new BigDecimal("200.00"));
        reservation.setStatus(ReservationStatus.PENDING_PAYMENT);
        reservation.setLines(new ArrayList<>());

        categorie = new CategorieBillet();
        categorie.setId(1L);

        ligne = new LigneReservation();
        ligne.setId(1L);
        ligne.setCategorieBillet(categorie);
        ligne.setQuantity(2);

        paiementResponse = new PaiementResponse();
        billetResponse = new BilletResponse();
    }

    @Test
    void createPaiementSuccessfully() {

        Long reservationId = 1L;

        reservation.setLines(List.of(ligne));

        when(request.getReservationId()).thenReturn(reservationId);
        when(request.getAmount()).thenReturn(new BigDecimal("200.00"));

        when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(reservation));

        when(categorieBilletRepository.findById(1L))
                .thenReturn(Optional.of(categorie));

        when(billetRepository.save(any(Billet.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(billetMapper.toResponse(any(Billet.class)))
                .thenReturn(billetResponse);

        when(paiementRepository.save(any(Paiement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(paiementMapper.toResponse(any(Paiement.class)))
                .thenReturn(paiementResponse);

        PaiementResponse result = paiementService.createPaiement(request);

        assertNotNull(result);
        assertSame(paiementResponse, result);

        assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus());

        assertNotNull(result.getBillets());
        assertEquals(2, result.getBillets().size());

        verify(reservationRepository).findById(reservationId);
        verify(categorieBilletRepository).findById(1L);
        verify(billetRepository, times(2)).save(any(Billet.class));
        verify(paiementRepository).save(any(Paiement.class));
        verify(paiementMapper).toResponse(any(Paiement.class));
    }

    @Test
    void createPaiementWhenReservationNotFound() {

        Long reservationId = 1L;

        when(request.getReservationId()).thenReturn(reservationId);

        when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.empty());

        assertThrows(
                ReservationNotFound.class,
                () -> paiementService.createPaiement(request)
        );

        verify(reservationRepository).findById(reservationId);
        verify(paiementRepository, never()).save(any(Paiement.class));
    }

    @Test
    void createPaiementWhenReservationExpired() {

        Long reservationId = 1L;

        reservation.setStatus(ReservationStatus.EXPIRED);

        when(request.getReservationId()).thenReturn(reservationId);

        when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(reservation));

        assertThrows(
                ReservationExpired.class,
                () -> paiementService.createPaiement(request)
        );

        verify(paiementRepository, never()).save(any(Paiement.class));
    }

    @Test
    void createPaiementWhenReservationStatusIsNotPendingPayment() {

        Long reservationId = 1L;

        reservation.setStatus(ReservationStatus.CONFIRMED);

        when(request.getReservationId()).thenReturn(reservationId);

        when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(reservation));

        assertThrows(
                ConflictException.class,
                () -> paiementService.createPaiement(request)
        );

        verify(paiementRepository, never()).save(any(Paiement.class));
    }

    @Test
    void createPaiementWhenAmountIsIncorrect() {

        Long reservationId = 1L;

        when(request.getReservationId()).thenReturn(reservationId);
        when(request.getAmount()).thenReturn(new BigDecimal("150.00"));

        when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(reservation));

        assertThrows(
                ValidationException.class,
                () -> paiementService.createPaiement(request)
        );

        assertEquals(ReservationStatus.PENDING_PAYMENT, reservation.getStatus());

        verify(paiementRepository, never()).save(any(Paiement.class));
        verify(billetRepository, never()).save(any(Billet.class));
    }

    @Test
    void addBilletsSuccessfully() {

        ligne.setQuantity(2);
        reservation.setLines(List.of(ligne));

        when(categorieBilletRepository.findById(1L))
                .thenReturn(Optional.of(categorie));

        when(billetRepository.save(any(Billet.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(billetMapper.toResponse(any(Billet.class)))
                .thenReturn(billetResponse);

        List<BilletResponse> result = paiementService.addBillets(reservation);

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(categorieBilletRepository).findById(1L);
        verify(billetRepository, times(2)).save(any(Billet.class));
        verify(billetMapper, times(2)).toResponse(any(Billet.class));
    }

    @Test
    void addBilletsWhenCategorieNotFound() {

        reservation.setLines(List.of(ligne));

        when(categorieBilletRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                CategorieNotFound.class,
                () -> paiementService.addBillets(reservation)
        );

        verify(categorieBilletRepository).findById(1L);
        verify(billetRepository, never()).save(any(Billet.class));
    }

    @Test
    void addBilletsWhenQuantityIsZero() {

        ligne.setQuantity(0);
        reservation.setLines(List.of(ligne));

        when(categorieBilletRepository.findById(1L))
                .thenReturn(Optional.of(categorie));

        List<BilletResponse> result = paiementService.addBillets(reservation);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(categorieBilletRepository).findById(1L);
        verify(billetRepository, never()).save(any(Billet.class));
        verify(billetMapper, never()).toResponse(any(Billet.class));
    }

    @Test
    void addBilletsWhenReservationHasNoLines() {

        reservation.setLines(new ArrayList<>());

        List<BilletResponse> result = paiementService.addBillets(reservation);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verifyNoInteractions(categorieBilletRepository);
        verifyNoInteractions(billetRepository);
        verifyNoInteractions(billetMapper);
    }
}
