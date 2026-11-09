package ma.eventpro.dto.response;

import lombok.*;
import ma.eventpro.entity.enums.ReservationStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationResponse {
    private Long id ;
    private UUID clientId;
    private String reference;
    private Instant creationDate;
    private Instant expirationDate;
    private ReservationStatus status;
    private BigDecimal totalAmount;
    private String promoCode;
    private List<LigneReservationResponse> lines;
}
