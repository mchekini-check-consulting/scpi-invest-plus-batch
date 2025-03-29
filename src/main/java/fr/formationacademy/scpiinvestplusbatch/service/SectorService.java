package fr.formationacademy.scpiinvestplusbatch.service;

import fr.formationacademy.scpiinvestplusbatch.dto.SectorRequest;
import fr.formationacademy.scpiinvestplusbatch.entity.Scpi;
import fr.formationacademy.scpiinvestplusbatch.entity.Sector;
import fr.formationacademy.scpiinvestplusbatch.entity.SectorId;
import fr.formationacademy.scpiinvestplusbatch.mapper.SectorMapper;
import fr.formationacademy.scpiinvestplusbatch.repository.SectorRepository;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Slf4j
public class SectorService{

    private final SectorRepository sectorRepository;
    private final SectorMapper sectorMapper;

    public SectorService(SectorRepository sectorRepository, SectorMapper sectorMapper) {
        this.sectorRepository = sectorRepository;
        this.sectorMapper = sectorMapper;
    }

    @Transactional("appTransactionManager")
    public List<Sector> createSectors(String sectorData, Scpi scpi) {
        if (StringUtils.isBlank(sectorData)) {
            log.debug("Aucun secteur fourni pour la SCPI: {}", scpi.getName());
            return Collections.emptyList();
        }

        List<Sector> newSectors = parseSectors(sectorData, scpi);
        if (newSectors.isEmpty()) {
            log.debug("Aucun secteur valide créé pour la SCPI: {}", scpi.getName());
            return Collections.emptyList();
        }

        List<Sector> existingSectors = sectorRepository.findByScpiId(scpi.getId());
        List<SectorRequest> newSectorRequests = sectorMapper.toRequestSectorList(newSectors);

        if (isSameSector(existingSectors, newSectorRequests)) {
            return existingSectors;
        }

        return newSectors;
    }

    private List<Sector> parseSectors(String sectorData, Scpi scpi) {
        String[] tokens = sectorData.split(",");
        if (tokens.length % 2 != 0) {
            log.error("Données de secteur incorrectes pour la SCPI {} : {}", scpi.getName(), sectorData);
            return Collections.emptyList();
        }

        return IntStream.range(0, tokens.length / 2)
                .mapToObj(i -> parseSector(tokens[i * 2].trim(), tokens[i * 2 + 1].trim(), scpi))
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<Sector> parseSector(String name, String percentageStr, Scpi scpi) {
        try {
            BigDecimal percentage = new BigDecimal(percentageStr);
            if (percentage.compareTo(BigDecimal.ZERO) < 0 || percentage.compareTo(BigDecimal.valueOf(100)) > 0) {
                log.debug("Pourcentage invalide pour {}: {}%", name, percentage);
                return Optional.empty();
            }
            return Optional.of(new Sector(new SectorId(scpi.getId(), name), percentage, scpi));
        } catch (NumberFormatException e) {
            log.error("Erreur de parsing pour le secteur: {}", percentageStr, e);
            return Optional.empty();
        }
    }

    public void saveSectors(List<Sector> sectors) {
        if (sectors == null || sectors.isEmpty()) {
            log.debug("Tentative de sauvegarde d'une liste vide ou nulle de secteurs.");
            return;
        }

        List<Sector> validSectors = sectors.stream()
                .filter(this::isValidSector)
                .toList();

        if (validSectors.isEmpty()) {
            log.debug("Aucun secteur valide à sauvegarder.");
            return;
        }

        try {
            sectorRepository.saveAll(validSectors);
        } catch (Exception e) {
            log.error("Erreur lors de la sauvegarde des secteurs : {}", e.getMessage(), e);
            throw new RuntimeException("Impossible d'enregistrer les secteurs", e);
        }
    }

    private boolean isValidSector(Sector sector) {
        if (sector == null || sector.getId() == null || StringUtils.isBlank(sector.getId().getName()) || sector.getId().getScpiId() == null) {
            log.debug("Secteur invalide : clé composite incorrecte {}", sector);
            return false;
        }

        if (sector.getSectorPercentage() == null || sector.getSectorPercentage().compareTo(BigDecimal.ZERO) < 0 || sector.getSectorPercentage().compareTo(BigDecimal.valueOf(100)) > 0) {
            log.debug("Secteur invalide : pourcentage incorrect {}", sector);
            return false;
        }

        return true;
    }

    public boolean isSameSector(List<Sector> existingSectors, List<SectorRequest> newSectorRequests) {
        if (existingSectors.size() != newSectorRequests.size()) {
            return false;
        }

        Map<String, BigDecimal> existingMap = existingSectors.stream()
                .collect(Collectors.toMap(sector -> sector.getId().getName(), Sector::getSectorPercentage));

        return newSectorRequests.stream().allMatch(dto ->
                existingMap.containsKey(dto.getName()) &&
                        existingMap.get(dto.getName()).compareTo(dto.getSectorPercentage()) == 0
        );
    }

}
