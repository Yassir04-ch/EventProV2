package ma.eventpro.dto.response;

import lombok.*;
import ma.eventpro.entity.enums.BilletStatus;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BilletResponse {
    private Long id;
    private String code;
    private BilletStatus status;
    private Instant issueDate;
    private String categorieName;
    private Long evenementId;
    private String reservationReference;
}
