package ma.eventpro.mapper;

import ma.eventpro.dto.response.BilletResponse;
import ma.eventpro.entity.Billet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BilletMapper {
    @Mapping(source = "lineReservation.categorieBillet.name", target = "categorieName")
    @Mapping(source = "lineReservation.categorieBillet.evenement.id", target = "evenementId")
    @Mapping(source = "lineReservation.reservation.reference", target = "reservationReference")
    BilletResponse toResponse(Billet billet);
}