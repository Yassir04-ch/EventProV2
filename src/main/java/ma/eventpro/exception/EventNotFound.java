package ma.eventpro.exception;

public class EventNotFound extends ResourceNotFoundException {
  public EventNotFound(String message) {
    super(message);
  }
}
