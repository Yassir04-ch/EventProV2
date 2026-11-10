package ma.eventpro.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ma.eventpro.entity.LigneReservation;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationCreateRequest {

    @NotNull(message = "client et obligatoire")
    private UUID clientId;

    private  String promoCode;

    @NotEmpty(message = "réservation doit contenir au moins une ligne")
    private List<LigneReservationRequest> linesreserv;
}
