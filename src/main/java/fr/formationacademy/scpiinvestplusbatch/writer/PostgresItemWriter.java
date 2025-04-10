package fr.formationacademy.scpiinvestplusbatch.writer;

import fr.formationacademy.scpiinvestplusbatch.dto.BatchDataDto;
import fr.formationacademy.scpiinvestplusbatch.service.BatchService;
import fr.formationacademy.scpiinvestplusbatch.service.LocationService;
import fr.formationacademy.scpiinvestplusbatch.service.SectorService;
import fr.formationacademy.scpiinvestplusbatch.service.StatYearService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Slf4j
public class PostgresItemWriter implements ItemWriter<BatchDataDto> {

    private final BatchService batchService;
    private final LocationService locationService;
    private final SectorService sectorService;
    private final StatYearService statYearService;

    public PostgresItemWriter(BatchService batchService, LocationService locationService, SectorService sectorService, StatYearService statYearService) {
        this.batchService = batchService;
        this.locationService = locationService;
        this.sectorService = sectorService;
        this.statYearService = statYearService;
    }

    @Transactional
    @Override
    public void write(Chunk<? extends BatchDataDto> items) {
        if (items.isEmpty()) return;

        List<BatchDataDto> batchDataList = items.getItems().stream()
                .map(item -> (BatchDataDto) item)
                .toList();
        batchService.saveOrUpdateBatchData(batchDataList);
        batchDataList.forEach(batchData -> {
            if (batchData.getLocations() != null) {
                locationService.saveLocations(batchData.getLocations());
            }
            if (batchData.getSectors() != null) {
                sectorService.saveSectors(batchData.getSectors());
            }
            if (batchData.getStatYears() != null) {
                statYearService.saveStatYears(batchData.getStatYears());
            }
        });

        log.info("{} SCPI enregistrées en base", batchDataList.size());
    }
}
