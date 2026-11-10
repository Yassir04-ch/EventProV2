package ma.eventpro.dto.response;

import lombok.*;
import ma.eventpro.entity.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaiementResponse {
    private Long id;
    private String reference;
    private BigDecimal amount;
    private Instant paymentDate;
    private PaymentStatus status;
    private Long reservationId;
    private List<BilletResponse> billets;
}
