package ma.eventpro.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import ma.eventpro.entity.enums.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionCreateRequest {

    @NotNull(message = "evenement est obligatoire")
    private Long evenementId;

    @NotBlank(message = "code est obligatoire")
    private String code;

    @NotNull(message = "type de réduction est obligatoire")
    private DiscountType discountType;

    @NotNull(message = "valeur de la réduction est obligatoire")
    @Positive(message = "valeur doit être supérieure à 0")
    private BigDecimal discountValue;

    @NotNull(message = "date de début est obligatoire")
    private LocalDateTime startDate;

    @NotNull(message = "date de fin est obligatoire")
    private LocalDateTime endDate;

    @NotNull(message = "limite d'utilisation est obligatoire")
    @Min(value = 1, message = "limite d'utilisation doit étre au moins 1")
    private Integer usageLimit;
}
