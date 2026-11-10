package ma.eventpro.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaiementCreateRequest {
    @NotNull(message = "réservation est obligatoire")
    private Long reservationId;

    @NotNull(message = "le montant et oblégatoire")
    @Positive
    private BigDecimal amount;

}
