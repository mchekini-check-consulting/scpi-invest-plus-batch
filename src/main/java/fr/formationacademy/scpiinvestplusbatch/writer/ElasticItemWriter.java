package fr.formationacademy.scpiinvestplusbatch.writer;

import fr.formationacademy.scpiinvestplusbatch.entity.postgrs.Scpi;
import fr.formationacademy.scpiinvestplusbatch.repository.elastic.ScpiElasticRepository;
import fr.formationacademy.scpiinvestplusbatch.service.BatchService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class ElasticItemWriter implements ItemWriter<Scpi> {

    private final ScpiElasticRepository elasticRepository;
    private final BatchService batchService;

    public ElasticItemWriter(ScpiElasticRepository elasticRepository, BatchService batchService) {
        this.elasticRepository = elasticRepository;
        this.batchService = batchService;
    }

    @Override
    public void write(@NonNull Chunk<? extends Scpi> items) throws Exception {
        if (!items.isEmpty()) {
            List<? extends Scpi> scpis = items.getItems();
            for (Scpi scpi : scpis) {
                batchService.saveToElastic(scpi);
            }
            long documentCount = elasticRepository.count();
            log.info("Nombre total des Scpis sauvegardées dans la collection ElasticSearch : {}", documentCount);
        }
    }
}
