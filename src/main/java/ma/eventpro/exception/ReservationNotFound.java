package ma.eventpro.exception;

public class ReservationNotFound extends ResourceNotFoundException {
    public ReservationNotFound(String message) {
        super(message);
    }
}
