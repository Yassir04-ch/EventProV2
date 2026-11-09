package ma.eventpro.mapper;

import ma.eventpro.dto.response.PaiementResponse;
import ma.eventpro.entity.Paiement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaiementMapper {
    @Mapping(source = "reservation.id" ,target = "reservationId")
    @Mapping(target = "billets" , ignore = true)
    PaiementResponse toResponse(Paiement paiement);
}
