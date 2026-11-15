package ma.eventpro.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventUpdateRequest {
    @Size(max = 255)
    private String title;
    private String description;
    private String location;
    @Future(message = "La date de début doit étre dans le futur")
    private Instant startDate;
    @Future(message = "La date de fin doit étre dans le futur")
    private Instant endDate;
    private Instant salesOpeningDate;
    private Instant salesClosingDate;
}