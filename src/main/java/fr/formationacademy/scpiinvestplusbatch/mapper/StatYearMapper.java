package fr.formationacademy.scpiinvestplusbatch.mapper;

import fr.formationacademy.scpiinvestplusbatch.dto.StatYearDtoOut;
import fr.formationacademy.scpiinvestplusbatch.entity.StatYear;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StatYearMapper {
    StatYearDtoOut toDTO(StatYear statYear);
}
