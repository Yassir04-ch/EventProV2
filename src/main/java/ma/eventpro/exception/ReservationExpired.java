package ma.eventpro.exception;

public class ReservationExpired extends RuntimeException {
    public ReservationExpired(String message) {
        super(message);
    }
}
