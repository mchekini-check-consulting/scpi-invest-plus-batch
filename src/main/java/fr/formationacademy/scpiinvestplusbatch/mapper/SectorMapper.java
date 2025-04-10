package fr.formationacademy.scpiinvestplusbatch.mapper;

import fr.formationacademy.scpiinvestplusbatch.dto.SectorRequest;
import fr.formationacademy.scpiinvestplusbatch.entity.elastic.SectorDocument;
import fr.formationacademy.scpiinvestplusbatch.entity.postgres.Scpi;
import fr.formationacademy.scpiinvestplusbatch.entity.postgres.Sector;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper(componentModel = "spring")
@Component
public interface SectorMapper {
    default SectorRequest toRequest(Sector sector) {
        return new SectorRequest(
                sector.getId().getName(),
                sector.getSectorPercentage(),
                sector.getId().getScpiId()
        );
    }

    default List<SectorRequest> toRequestSectorList(List<Sector> sectors) {
        return sectors.stream()
                .map(this::toRequest)
                .toList();
    }

    default List<SectorDocument> mapSectors(Scpi scpi) {
        if (scpi.getSectors() == null) return null;

        return scpi.getSectors().stream()
                .map(sector -> SectorDocument.builder()
                        .name(sector.getId().getName())
                        .sectorPercentage(sector.getSectorPercentage())
                        .build())
                .toList();
    }

}
