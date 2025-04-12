package fr.formationacademy.scpiinvestplusbatch.service;

import fr.formationacademy.scpiinvestplusbatch.entity.elastic.SectorDominant;
import fr.formationacademy.scpiinvestplusbatch.entity.postgres.Scpi;
import fr.formationacademy.scpiinvestplusbatch.entity.postgres.Sector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class SectorService {

    public SectorDominant getSectorDominant(Scpi scpi) {
        return scpi.getSectors() != null && !scpi.getSectors().isEmpty()
                ? scpi.getSectors().stream()
                .filter(sec -> sec.getId() != null && sec.getId().getName() != null)
                .max(Comparator.comparing(Sector::getSectorPercentage))
                .map(sec -> new SectorDominant(sec.getId().getName(), sec.getSectorPercentage()))
                .orElse(null)
                : null;
    }
}
