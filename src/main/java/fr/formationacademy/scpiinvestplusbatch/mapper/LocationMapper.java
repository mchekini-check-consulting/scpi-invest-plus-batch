package fr.formationacademy.scpiinvestplusbatch.mapper;

import fr.formationacademy.scpiinvestplusbatch.dto.LocationRequest;
import fr.formationacademy.scpiinvestplusbatch.entity.elastic.LocationDocument;
import fr.formationacademy.scpiinvestplusbatch.entity.postgres.Location;
import fr.formationacademy.scpiinvestplusbatch.entity.postgres.Scpi;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper(componentModel = "spring")
@Component
public interface LocationMapper {
    default LocationRequest toRequest(Location location) {
        return new LocationRequest(
                location.getId().getCountry(),
                location.getCountryPercentage(),
                location.getId().getScpiId()
        );
    }

    default List<LocationRequest> toRequestLocationList(List<Location> locations) {
        return locations.stream()
                .map(this::toRequest)
                .toList();
    }

    default List<LocationDocument> mapLocations(Scpi scpi) {
        if (scpi.getLocations() == null) return null;

        return scpi.getLocations().stream()
                .map(location -> LocationDocument.builder()
                        .id(location.getScpi().getId())
                        .countryPercentage(location.getCountryPercentage())
                        .build())
                .toList();
    }



}
