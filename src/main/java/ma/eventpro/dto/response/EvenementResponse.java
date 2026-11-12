package ma.eventpro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.eventpro.entity.enums.EventStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvenementResponse {
    private Long id;
    private String title;
    private String description;
    private String location;
    private Instant startDate;
    private Instant endDate;
    private Instant salesOpeningDate;
    private Instant salesClosingDate;
    private EventStatus status;
    private UUID organisateurId;
}