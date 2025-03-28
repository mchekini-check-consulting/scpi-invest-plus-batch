package fr.formationacademy.scpiinvestplusbatch.mapper;

import fr.formationacademy.scpiinvestplusbatch.dto.LocationDtoOut;
import fr.formationacademy.scpiinvestplusbatch.entity.Location;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    LocationDtoOut toDTO(Location location);
    Location toEntity(LocationDtoOut locationDTO);
}
