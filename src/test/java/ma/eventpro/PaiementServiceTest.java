package ma.eventpro;

import ma.eventpro.dto.request.PaiementCreateRequest;
import ma.eventpro.dto.response.PaiementResponse;
import ma.eventpro.entity.*;
import ma.eventpro.entity.enums.ReservationStatus;
import ma.eventpro.exception.ReservationExpired;
import ma.eventpro.exception.ReservationNotFound;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaiementServiceTest {
    @Mock
    PaiementRepository paiementRepository;
    @Mock
    ReservationRepository reservationRepository;
    @Mock
    BilletRepository billetRepository;
    @Mock
    CategorieBilletRepository categorieBilletRepository;
    @Mock
    PaiementMapper paiementMapper;

    @InjectMocks
    PaiementService paiementService;

    private Reservation reservation;
    private Paiement paiement;
    private PaiementResponse response;
    private PaiementCreateRequest request;
    private CategorieBillet categorie;
    private LigneReservation ligne;

    @BeforeEach
    void setUp() {
        reservation = new Reservation();
        paiement = new Paiement();
        response = new PaiementResponse();
        request = mock(PaiementCreateRequest.class);
        categorie = new CategorieBillet();
        ligne = new LigneReservation();

        reservation.setTotalAmount(new BigDecimal("200.00"));
        reservation.setStatus(ReservationStatus.PENDING_PAYMENT);
        reservation.setLines(new ArrayList<>());
    }

    @Test
    void createPaiement(){
        Long reservationId = 1L;
        when(request.getReservationId()).thenReturn(reservationId);
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(paiementMapper.toResponse(any(Paiement.class))).thenReturn(response);
        when(request.getAmount()).thenReturn(new BigDecimal("200.00"));
        PaiementResponse result = paiementService.createPaiement(request);
        assertNotNull(result);
        assertSame(response , result);
        assertEquals(ReservationStatus.CONFIRMED , reservation.getStatus());
        verify(reservationRepository).findById(reservationId);
        verify(paiementMapper).toResponse(any(Paiement.class));
    }
    @Test
    void createPaiementWhenReservationNotFound(){
        Long reservationId = 1L;
        when(request.getReservationId()).thenReturn(reservationId);
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());
        assertThrows(ReservationNotFound.class ,()-> paiementService.createPaiement(request));
        verify(reservationRepository).findById(reservationId);
        verify(paiementRepository,never()).save(any(Paiement.class));
    }
    @Test
    void createPaiementWhenReservationExpired(){
        Long reservationId = 1L;
        when(request.getReservationId()).thenReturn(reservationId);
        reservation.setStatus(ReservationStatus.EXPIRED);
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        assertThrows(ReservationExpired.class ,()->paiementService.createPaiement(request));
        verify(reservationRepository).findById(reservationId);
        verify(paiementRepository,never()).save(any(Paiement.class));
    }
    @Test
    void addBillets(){
        categorie.setId(1L);
        ligne.setCategorieBillet(categorie);
        ligne.setQuantity(2);
        reservation.setLines(List.of(ligne));
        when(categorieBilletRepository.findById(1L)).thenReturn(Optional.of(categorie));
        paiementService.addBillets(reservation);
        verify(categorieBilletRepository).findById(1L);
        verify(billetRepository,times(2)).save(any(Billet.class));
    }
}
