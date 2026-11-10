package ma.eventpro.mapper;

import ma.eventpro.dto.request.CategorieBilletRequest;
import ma.eventpro.dto.request.CategorieBilletUpdateRequest;
import ma.eventpro.dto.response.CategorieBilletResponse;
import ma.eventpro.entity.CategorieBillet;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CategorieBilletMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "evenement", ignore = true)
    @Mapping(target = "placesReservees", ignore = true)
    CategorieBillet toEntity(CategorieBilletRequest request);

    CategorieBilletResponse toResponse(CategorieBillet categorie);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "evenement", ignore = true)
    @Mapping(target = "placesReservees", ignore = true)
    void updateEntity(CategorieBilletUpdateRequest request, @MappingTarget CategorieBillet categorie);
}
