package ma.eventpro.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UtilisateurUpdateRequest {
    @Size(min = 2 ,message = "nom minimum deux caracter")
    private String nom;
    @Email(message = "email invalide")
    private String email;
    private String phone;
    private String organisationName;
}
