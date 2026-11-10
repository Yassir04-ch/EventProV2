package ma.eventpro.exception;

public class ClientNotFound extends ResourceNotFoundException {
    public ClientNotFound(String message) {
        super(message);
    }
}
