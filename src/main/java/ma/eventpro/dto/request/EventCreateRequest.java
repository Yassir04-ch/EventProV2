package ma.eventpro.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventCreateRequest {
    @NotNull(message = "L'organisateur est obligatoire")
    private UUID organisateurId;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 255)
    private String title;

    private String description;

    @NotBlank(message = "Le lieu est obligatoire")
    private String location;

    @NotNull(message = "La date de début est obligatoire")
    private Instant startDate;

    @NotNull(message = "La date de fin est obligatoire")
    private Instant endDate;

    private Instant salesOpeningDate;

    private Instant salesClosingDate;

    @NotEmpty(message = "Un événement doit avoir au moins une catégorie de billets")
    @Valid
    private List<CategorieBilletRequest> categories;
}
