package fr.formationacademy.scpiinvestplusbatch.writer;

import fr.formationacademy.scpiinvestplusbatch.entity.postgres.Scpi;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PostgresItemWriter implements ItemWriter<Scpi> {

    private final JpaItemWriter<Scpi> delegate;

    public PostgresItemWriter(EntityManagerFactory entityManagerFactory) {
        this.delegate = new JpaItemWriter<>();
        this.delegate.setEntityManagerFactory(entityManagerFactory);
    }

    @Override
    public void write(Chunk<? extends Scpi> items) throws Exception {
        if (items.isEmpty()) {
            log.info("Aucune SCPI à traiter.");
            return;
        }

        log.info("Insertion/Mise à jour de {} SCPIs.", items.size());
        delegate.write(items);
    }
}
