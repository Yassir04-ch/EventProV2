package ma.eventpro.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class OrganisateurCreateRequest extends  UtilisateurCreateRequest{

    @NotBlank(message = "Le nom de l'organisation est obligatoire")
    private String organisationName;
}
