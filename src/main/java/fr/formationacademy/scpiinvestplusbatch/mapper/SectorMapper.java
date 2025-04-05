package fr.formationacademy.scpiinvestplusbatch.mapper;

import fr.formationacademy.scpiinvestplusbatch.dto.SectorRequest;
import fr.formationacademy.scpiinvestplusbatch.entity.postgrs.Sector;
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

}
