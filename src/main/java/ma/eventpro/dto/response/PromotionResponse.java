package ma.eventpro.dto.response;

import lombok.*;
import ma.eventpro.entity.enums.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionResponse {
    private Long id;
    private String code;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer usageLimit;
    private Integer usageCount;
    private Long evenementId;
}
