package fr.formationacademy.scpiinvestplusbatch.writer.shared;

import fr.formationacademy.scpiinvestplusbatch.entity.postgres.Scpi;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Configuration
public class CompositeWriter {

    private final ItemWriter<Scpi> mongoItemWriter;
    private final ItemWriter<Scpi> elasticItemWriter;

    public CompositeWriter(ItemWriter<Scpi> mongoItemWriter, ItemWriter<Scpi> elasticItemWriter) {
        this.mongoItemWriter = mongoItemWriter;
        this.elasticItemWriter = elasticItemWriter;
    }

    @Bean
    public ItemWriter<Scpi> compositeWriter() {
        return items -> {
            List<Scpi> persistedScpis = items.getItems().stream()
                    .map(item -> (Scpi) item)
                    .toList();

            Chunk<Scpi> chunk = new Chunk<>(persistedScpis);

            CompletableFuture<Void> mongoFuture = CompletableFuture.runAsync(() -> {
                try {
                    mongoItemWriter.write(chunk);
                } catch (Exception e) {
                    throw new RuntimeException("Erreur lors de l'écriture Mongo", e);
                }
            });

            CompletableFuture<Void> elasticFuture = CompletableFuture.runAsync(() -> {
                try {
                    elasticItemWriter.write(chunk);
                } catch (Exception e) {
                    throw new RuntimeException("Erreur lors de l'écriture Elastic", e);
                }
            });

            CompletableFuture.allOf(mongoFuture, elasticFuture).join();
        };
    }
}