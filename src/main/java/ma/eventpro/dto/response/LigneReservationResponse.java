package ma.eventpro.dto.response;

import lombok.*;
import ma.eventpro.entity.Reservation;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneReservationResponse {
    private Long id ;
    private  Integer quantity;
    private Long categorieBilletId;
    private String categorieName;
    private BigDecimal fixedUnitPrice;
    private BigDecimal subtotal;

}
