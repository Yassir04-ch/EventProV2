package ma.eventpro.mapper;

import ma.eventpro.dto.response.ReservationResponse;
import ma.eventpro.entity.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring" , uses = LigneReservationMapper.class
)
public interface ReservationMapper {
    @Mapping(source = "client.id", target = "clientId")
    @Mapping(source = "promotion.code", target = "promoCode")
    ReservationResponse toResponse(Reservation reservation);

}
