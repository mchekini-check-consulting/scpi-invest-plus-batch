package fr.formationacademy.scpiinvestplusbatch.writer;

import fr.formationacademy.scpiinvestplusbatch.entity.mongo.ScpiMongo;
import fr.formationacademy.scpiinvestplusbatch.repository.mongo.ScpiMongoRepository;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class MongoItemWriter implements ItemWriter<ScpiMongo> {

    private final ScpiMongoRepository mongoRepository;

    public MongoItemWriter(ScpiMongoRepository mongoRepository) {
        this.mongoRepository = mongoRepository;
    }

    @Override
    public void write(@NonNull Chunk<? extends ScpiMongo> items) {
        if (!items.isEmpty()) {
            List<? extends ScpiMongo> scpis = items.getItems();
            for (ScpiMongo scpiMongo : scpis) {
                Optional<ScpiMongo> existing = mongoRepository.findByName(scpiMongo.getName());
                existing.ifPresent(mongo -> scpiMongo.setScpiId(mongo.getScpiId()));
                mongoRepository.save(scpiMongo);
            }
            long documentCount = mongoRepository.count();
            log.info("Nombre total des SCPI dans la collection MongoDB : {}", documentCount);
        }
    }

}