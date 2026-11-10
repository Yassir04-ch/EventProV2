package ma.eventpro.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RemboursementCreateRequest {
    @NotNull(message = "Paiement et obligatoire")
    private Long paiementId;
    @NotNull(message = "Cleint et obligatoire")
    private UUID clientId;
}
