package ma.eventpro.exception;

public class PaiementNotFound extends ResourceNotFoundException {
    public PaiementNotFound(String message) {
        super(message);
    }
}
