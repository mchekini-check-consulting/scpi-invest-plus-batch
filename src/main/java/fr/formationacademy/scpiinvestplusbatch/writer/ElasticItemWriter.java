package fr.formationacademy.scpiinvestplusbatch.writer;

import fr.formationacademy.scpiinvestplusbatch.entity.elastic.ScpiDocument;
import fr.formationacademy.scpiinvestplusbatch.repository.elastic.ScpiElasticRepository;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class ElasticItemWriter implements ItemWriter<ScpiDocument> {

    private final ScpiElasticRepository elasticRepository;

    public ElasticItemWriter(ScpiElasticRepository elasticRepository) {
        this.elasticRepository = elasticRepository;
    }

    @Override
    public void write(@NonNull Chunk<? extends ScpiDocument> items) {
        if (!items.isEmpty()) {
            List<? extends ScpiDocument> scpis = items.getItems();
            for (ScpiDocument scpiDocument : scpis) {
                Optional<ScpiDocument> existing = elasticRepository.findByName(scpiDocument.getName());
                existing.ifPresent(document -> scpiDocument.setScpiId(document.getScpiId()));
                elasticRepository.save(scpiDocument);
            }
            long documentCount = elasticRepository.count();
            log.info("Nombre total des SCPI dans ElasticSearch : {}", documentCount);
        }
    }
}