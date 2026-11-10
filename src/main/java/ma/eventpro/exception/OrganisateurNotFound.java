package ma.eventpro.exception;

public class OrganisateurNotFound extends ResourceNotFoundException  {
    public OrganisateurNotFound(String message) {
        super(message);
    }
}
