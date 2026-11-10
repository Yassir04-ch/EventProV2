package ma.eventpro.dto.response;

import lombok.*;
import ma.eventpro.entity.Paiement;
import ma.eventpro.entity.enums.RefundStatus;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RemboursementResponse {
    private Long id;
    private BigDecimal amount;
    private Instant requestDate ;
    private RefundStatus status;
    private Long paymentId;
    private Long reservationId;
}
