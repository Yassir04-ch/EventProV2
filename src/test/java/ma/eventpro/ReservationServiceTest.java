package ma.eventpro;

import ma.eventpro.dto.request.LigneReservationRequest;
import ma.eventpro.dto.request.ReservationCreateRequest;
import ma.eventpro.dto.response.ReservationResponse;
import ma.eventpro.entity.*;
import ma.eventpro.entity.enums.EventStatus;
import ma.eventpro.mapper.ReservationMapper;
import ma.eventpro.repository.CategorieBilletRepository;
import ma.eventpro.repository.ClientRepository;
import ma.eventpro.repository.LigneReservationRepository;
import ma.eventpro.repository.ReservationRepository;
import ma.eventpro.service.PromotionService;
import ma.eventpro.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @Mock
    ReservationRepository reservationRepository;
    @Mock
    LigneReservationRepository ligneReservationRepository;
    @Mock
    CategorieBilletRepository categorieBilletRepository;
    @Mock
    ClientRepository clientRepository;
    @Mock
    PromotionService promotionService;
    @Mock
    ReservationMapper reservationMapper;

    @InjectMocks
    ReservationService reservationService;

    private static final UUID CLIENT_ID = UUID.randomUUID();
    private static final Long EVENT_ID = 10L;

    private Client client;
    private ReservationResponse response;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setId(CLIENT_ID);
        response = new ReservationResponse();
    }

    private Evenement evenement(Long id, EventStatus status) {
        Evenement evenement = new Evenement();
        evenement.setId(EVENT_ID);
        evenement.setStatus(status);
        return evenement;
    }

    private CategorieBillet categorie(Long id, Evenement evenement, int total, int reservees, int vendues, int quota, String prix) {
        CategorieBillet categorieBillet = new CategorieBillet();
        categorieBillet.setId(id);
        categorieBillet.setName("CAT-" + id);
        categorieBillet.setEvenement(evenement);
        categorieBillet.setQuantiteTotale(total);
        categorieBillet.setPlacesReservees(reservees);
        categorieBillet.setQuotaMaxParClient(quota);
        categorieBillet.setPrixUnitaire(new BigDecimal(prix));
        return categorieBillet;
    }

    @Test
    void createReservationTest() {
        Evenement event = evenement(EVENT_ID, EventStatus.PUBLISHED);

        CategorieBillet category = categorie(1L, event, 100, 0, 0, 5, "100.00");

        ReservationCreateRequest request = new ReservationCreateRequest();
        request.setClientId(CLIENT_ID);
        request.setPromoCode(null);

        LigneReservationRequest lineRequest = new LigneReservationRequest();
        lineRequest.setCategorieBilletId(1L);
        lineRequest.setQuantity(2);

        request.setLinesreserv(List.of(lineRequest));
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));
        when(categorieBilletRepository.findById(1L)).thenReturn(Optional.of(category));
        when(ligneReservationRepository.sumQuantityByClientAndCategorie(CLIENT_ID, 1L)).thenReturn(0L);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reservationMapper.toResponse(any(Reservation.class))).thenReturn(response);
        ReservationResponse result = reservationService.createReservation(request);
        assertSame(response, result);
        verify(reservationRepository).save(any(Reservation.class));
        verify(reservationMapper).toResponse(any(Reservation.class));
        assertEquals(2, category.getPlacesReservees());
    }
}
