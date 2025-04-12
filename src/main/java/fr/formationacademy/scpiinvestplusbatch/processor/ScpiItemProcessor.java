package fr.formationacademy.scpiinvestplusbatch.processor;

import fr.formationacademy.scpiinvestplusbatch.dto.BatchDataDto;
import fr.formationacademy.scpiinvestplusbatch.dto.ScpiDto;
import fr.formationacademy.scpiinvestplusbatch.entity.postgres.Scpi;
import fr.formationacademy.scpiinvestplusbatch.repository.postgres.ScpiRepository;
import fr.formationacademy.scpiinvestplusbatch.service.BatchService;
import fr.formationacademy.scpiinvestplusbatch.service.LocationService;
import fr.formationacademy.scpiinvestplusbatch.service.SectorService;
import fr.formationacademy.scpiinvestplusbatch.service.StatYearService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class ScpiItemProcessor implements ItemProcessor<BatchDataDto, Scpi> {

    private final ScpiRepository scpiRepository;
    private final BatchService batchService;
    private final LocationService locationService;
    private final SectorService sectorService;
    private final StatYearService statYearService;

    public final Map<String, Scpi> existingScpis = new HashMap<>();
    private final Set<String> scpisInCsv = new HashSet<>();

    public ScpiItemProcessor(ScpiRepository scpiRepository, BatchService batchService, LocationService locationService, SectorService sectorService, StatYearService statYearService) {
        this.scpiRepository = scpiRepository;
        this.batchService = batchService;
        this.locationService = locationService;
        this.sectorService = sectorService;
        this.statYearService = statYearService;

    }

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

    @Bean
    public CompositeItemProcessor<ScpiDto, Scpi> processor() {
        CompositeItemProcessor<ScpiDto, Scpi> compositeProcessor = new CompositeItemProcessor<>();

        ItemProcessor<ScpiDto, Scpi> conversionProcessor = scpiRequest -> {
            BatchDataDto batchData = batchService.convertToBatchData(scpiRequest);
            return process(batchData);
        };
        compositeProcessor.setDelegates(List.of(new EncodingCorrectionProcessor<>(), conversionProcessor));
        return compositeProcessor;
    }

}



