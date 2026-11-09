package ma.eventpro.exception;

public class PromotionNotFound extends ResourceNotFoundException {
    public PromotionNotFound(String message) {
        super(message);
    }
}
