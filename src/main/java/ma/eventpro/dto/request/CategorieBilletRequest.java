package ma.eventpro.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategorieBilletRequest {
    @NotBlank(message = "Le nom de la catégorie est obligatoire")
    private String name;

    @NotNull(message = "Le prix est obligatoire")
    @Positive(message = "Le prix doit être supérieur à 0")
    private BigDecimal prixUnitaire;

    @NotNull(message = "La quantité totale est obligatoire")
    @Min(value = 1, message = "La quantité totale au moins 1")
    private Integer quantiteTotale;

    @NotNull(message = "Le quota maximum est obligatoire")
    @Min(value = 1, message = "Le quota au moins 1")
    private Integer quotaMaxParClient;}
