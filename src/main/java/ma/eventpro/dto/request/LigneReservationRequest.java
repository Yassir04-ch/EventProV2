package ma.eventpro.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneReservationRequest {

    @NotNull(message = "categorie billet est obligatiore")
    private Long categorieBilletId;

    @NotNull(message ="Quantity et obligatiore")
    @Min(value = 1 , message = "quantity dois étre au moins 1")
    private Integer quantity;
}