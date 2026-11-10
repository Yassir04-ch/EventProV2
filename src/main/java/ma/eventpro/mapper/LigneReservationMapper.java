package ma.eventpro.mapper;

import ma.eventpro.dto.response.LigneReservationResponse;
import ma.eventpro.entity.LigneReservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LigneReservationMapper {
    @Mapping(source = "categorieBillet.id" ,target = "categorieBilletId")
    @Mapping(source = "categorieBillet.name", target = "categorieName")
    LigneReservationResponse toResponse(LigneReservation ligneReservation);
}
