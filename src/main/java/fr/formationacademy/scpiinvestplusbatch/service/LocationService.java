package fr.formationacademy.scpiinvestplusbatch.service;

import fr.formationacademy.scpiinvestplusbatch.entity.elastic.CountryDominant;
import fr.formationacademy.scpiinvestplusbatch.entity.postgres.Location;
import fr.formationacademy.scpiinvestplusbatch.entity.postgres.Scpi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@Slf4j
public class LocationService {

    public CountryDominant getCountryDominant(Scpi scpi) {
        return scpi.getLocations() != null && !scpi.getLocations().isEmpty()
                ? scpi.getLocations().stream()
                .filter(loc -> loc.getId() != null && loc.getId().getCountry() != null)
                .max(Comparator.comparing(Location::getCountryPercentage))
                .map(loc -> new CountryDominant(loc.getId().getCountry(), loc.getCountryPercentage()))
                .orElse(null)
                : null;
    }
}



