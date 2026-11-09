package ma.eventpro.scheduler;

import lombok.RequiredArgsConstructor;
import ma.eventpro.service.ReservationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationScheduler {

    private final ReservationService reservationService;

    @Scheduled(fixedRate = 6000)
    public void checkReservations(){
        reservationService.expireReservations();
    }
}
