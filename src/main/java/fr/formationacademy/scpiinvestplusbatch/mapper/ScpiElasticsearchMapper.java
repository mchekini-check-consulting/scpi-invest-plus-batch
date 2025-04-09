package fr.formationacademy.scpiinvestplusbatch.mapper;

import fr.formationacademy.scpiinvestplusbatch.entity.postgres.*;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ScpiElasticsearchMapper {

    default Integer map(LocationId id) {
        return id != null ? id.getScpiId() : null;
    }

    default Integer map(SectorId id) {
        return id != null ? id.getScpiId() : null;
    }

    default Integer map(StatYearId id) {
        return id != null ? id.getScpiId() : null;
    }

}
