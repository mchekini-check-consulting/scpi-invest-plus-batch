package fr.formationacademy.scpiinvestplusbatch.writer;

import fr.formationacademy.scpiinvestplusbatch.entity.postgres.Scpi;
import fr.formationacademy.scpiinvestplusbatch.repository.mongo.ScpiMongoRepository;
import fr.formationacademy.scpiinvestplusbatch.service.BatchService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class MongoItemWriter implements ItemWriter<Scpi> {

    private final ScpiMongoRepository mongoRepository;
    private final BatchService batchService;

    public MongoItemWriter(ScpiMongoRepository mongoRepository, BatchService batchService) {
        this.mongoRepository = mongoRepository;
        this.batchService = batchService;
    }

    @Override
    public void write(@NonNull Chunk<? extends Scpi> items) throws Exception {
        if (!items.isEmpty()) {
            List<? extends Scpi> scpis = items.getItems();
            for (Scpi scpi : scpis) {
                batchService.saveToMongo(scpi);
            }
            long documentCount = mongoRepository.count();
            log.info("Nombre total des Scpis sauvegardées dans la collection MongoDB : {}", documentCount);
        }
    }
}
