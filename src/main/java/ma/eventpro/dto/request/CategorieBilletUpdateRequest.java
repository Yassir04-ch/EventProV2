package ma.eventpro.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategorieBilletUpdateRequest {
    @NotBlank(message = "Le nom ne peut pas étre vide")
    private String name;

    @Positive(message = "Le prix doit étre supérieur à 0")
    private BigDecimal prixUnitaire;

    @Min(value = 1, message = "La quantité totale doit étre au moins 1")
    private Integer quantiteTotale;

    @Min(value = 1, message = "Le quota doit étre au moins 1")
    private Integer quotaMaxParClient;
}
