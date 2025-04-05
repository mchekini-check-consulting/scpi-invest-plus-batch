package fr.formationacademy.scpiinvestplusbatch.processor;


import fr.formationacademy.scpiinvestplusbatch.dto.BatchDataDto;
import fr.formationacademy.scpiinvestplusbatch.dto.ScpiDto;
import fr.formationacademy.scpiinvestplusbatch.entity.postgrs.Location;
import fr.formationacademy.scpiinvestplusbatch.entity.postgrs.Scpi;
import fr.formationacademy.scpiinvestplusbatch.entity.postgrs.Sector;
import fr.formationacademy.scpiinvestplusbatch.entity.postgrs.StatYear;
import fr.formationacademy.scpiinvestplusbatch.repository.postgres.ScpiRepository;
import fr.formationacademy.scpiinvestplusbatch.service.LocationService;
import fr.formationacademy.scpiinvestplusbatch.service.SectorService;
import fr.formationacademy.scpiinvestplusbatch.service.StatYearService;
import io.micrometer.common.lang.NonNull;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScpiItemProcessor implements ItemProcessor<BatchDataDto, Scpi> {

    private final ScpiRepository scpiRepository;
    private final LocationService locationService;
    private final SectorService sectorService;
    private final StatYearService statYearService;

    public final Map<String, Scpi> existingScpis = new HashMap<>();
    private final Set<String> scpisInCsv = new HashSet<>();


    @PostConstruct
    public void init() {
        log.info("Chargement des SCPIs existantes...");
        List<Scpi> scpis = scpiRepository.findAll();
        scpis.forEach(scpi -> existingScpis.put(scpi.getName(), scpi));
        log.info("Nombre de SCPIs existantes trouvées dans le cache des SCPIs: {}", existingScpis.size());
    }

    public void refreshCache() {
        log.debug("Rechargement du cache des SCPIs...");
        List<Scpi> scpis = scpiRepository.findAll();
        scpis.forEach(scpi -> existingScpis.put(scpi.getName(), scpi));
    }

    @Override
    public Scpi process(@NonNull BatchDataDto batchDataDto) {

        ScpiDto dto = batchDataDto.getScpiDto();
        if (dto == null || dto.getName() == null) {
            return null;
        }
        scpisInCsv.add(dto.getName());

        Scpi existingScpi = existingScpis.get(dto.getName());

        if (existingScpi != null && isSame(existingScpi, dto)) {
            return existingScpi;
        }

        Scpi scpi = createOrUpdateScpi(dto, existingScpi);

        List<Location> locations = locationService.createLocations(dto.getLocations(), scpi);
        locationService.saveLocations(locations);
        scpi.setLocations(locations);

        List<Sector> sectors = sectorService.createSectors(dto.getSectors(), scpi);
        sectorService.saveSectors(sectors);
        scpi.setSectors(sectors);

        List<StatYear> statYears = statYearService.createStatYears(dto, scpi);
        statYearService.saveStatYears(statYears);
        scpi.setStatYears(statYears);
        refreshCache();
        return scpi;
    }

    public Scpi createOrUpdateScpi(ScpiDto dto, Scpi existingScpi) {
        Scpi scpi = (existingScpi != null) ? existingScpi : new Scpi();
        scpi.setName(dto.getName());
        scpi.setMinimumSubscription(dto.getMinimumSubscription());
        scpi.setCapitalization(dto.getCapitalization());
        scpi.setManager(dto.getManager());
        scpi.setSubscriptionFees(dto.getSubscriptionFees());
        scpi.setManagementCosts(dto.getManagementCosts());
        scpi.setEnjoymentDelay(dto.getEnjoymentDelay());
        scpi.setIban(dto.getIban());
        scpi.setBic(dto.getBic());
        scpi.setScheduledPayment(dto.getScheduledPayment());
        scpi.setFrequencyPayment(dto.getFrequencyPayment());
        scpi.setCashback(dto.getCashback());
        scpi.setAdvertising(dto.getAdvertising());

        List<Location> locations = locationService.createLocations(dto.getLocations(), scpi);
        scpi.setLocations(locations);

        List<Sector> sectors = sectorService.createSectors(dto.getSectors(), scpi);
        scpi.setSectors(sectors);

        List<StatYear> statYears = statYearService.createStatYears(dto, scpi);
        scpi.setStatYears(statYears);

        existingScpis.put(scpi.getName(), scpi);

        return scpi;
    }

    public boolean isSame(Scpi existing, ScpiDto dto) {
        return Objects.equals(existing.getMinimumSubscription(), dto.getMinimumSubscription()) && Objects.equals(existing.getCapitalization(), dto.getCapitalization()) && Objects.equals(existing.getManager(), dto.getManager()) && Objects.equals(existing.getSubscriptionFees(), dto.getSubscriptionFees()) && Objects.equals(existing.getManagementCosts(), dto.getManagementCosts()) && Objects.equals(existing.getEnjoymentDelay(), dto.getEnjoymentDelay()) && Objects.equals(existing.getIban(), dto.getIban()) && Objects.equals(existing.getBic(), dto.getBic()) && Objects.equals(existing.getScheduledPayment(), dto.getScheduledPayment()) && Objects.equals(existing.getFrequencyPayment(), dto.getFrequencyPayment()) && Objects.equals(existing.getCashback(), dto.getCashback()) && Objects.equals(existing.getAdvertising(), dto.getAdvertising()) && Objects.equals(existing.getLocations(), dto.getLocations()) && Objects.equals(existing.getStatYears(), dto.getStatYear()) && Objects.equals(existing.getSectors(), dto.getSectors());
    }

}



