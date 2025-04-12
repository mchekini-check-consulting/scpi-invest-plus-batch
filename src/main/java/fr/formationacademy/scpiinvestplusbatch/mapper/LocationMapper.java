package fr.formationacademy.scpiinvestplusbatch.mapper;

import fr.formationacademy.scpiinvestplusbatch.entity.elastic.LocationDocument;
import fr.formationacademy.scpiinvestplusbatch.entity.postgres.Scpi;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper(componentModel = "spring")
@Component
public interface LocationMapper {

    default List<LocationDocument> mapLocations(Scpi scpi) {
        if (scpi.getLocations() == null) return null;

        return scpi.getLocations().stream()
                .map(location -> LocationDocument.builder()
                        .country(location.getId().getCountry())
                        .countryPercentage(location.getCountryPercentage())
                        .build())
                .toList();
    }
}
