package ma.eventpro.exception;

public class RemboursementNotFound extends ResourceNotFoundException{
    public RemboursementNotFound(String message) {
        super(message);
    }
}
