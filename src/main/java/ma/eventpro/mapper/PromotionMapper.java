package ma.eventpro.mapper;

import ma.eventpro.dto.request.PromotionCreateRequest;
import ma.eventpro.dto.response.PromotionResponse;
import ma.eventpro.entity.Promotion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PromotionMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usageCount", ignore = true)
    @Mapping(target = "evenement", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    Promotion toEntity(PromotionCreateRequest request);

    @Mapping(source = "evenement.id", target = "evenementId")
    PromotionResponse toResponse(Promotion promotion);
}

