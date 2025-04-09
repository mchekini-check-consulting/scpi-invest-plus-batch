package fr.formationacademy.scpiinvestplusbatch.config;

import fr.formationacademy.scpiinvestplusbatch.dto.BatchDataDto;
import fr.formationacademy.scpiinvestplusbatch.dto.ScpiDto;
import fr.formationacademy.scpiinvestplusbatch.entity.postgres.Scpi;
import fr.formationacademy.scpiinvestplusbatch.listener.BatchJobListener;
import fr.formationacademy.scpiinvestplusbatch.processor.EncodingCorrectionProcessor;
import fr.formationacademy.scpiinvestplusbatch.processor.ScpiItemProcessor;
import fr.formationacademy.scpiinvestplusbatch.reader.ScpiItemReader;
import fr.formationacademy.scpiinvestplusbatch.service.BatchService;
import fr.formationacademy.scpiinvestplusbatch.tasklet.DeleteMissingScpiTasklet;
import fr.formationacademy.scpiinvestplusbatch.writer.ElasticItemWriter;
import fr.formationacademy.scpiinvestplusbatch.writer.MongoItemWriter;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Configuration
@EnableBatchProcessing
@EnableScheduling
@Slf4j
public class BatchConfig {

    private final EntityManagerFactory entityManagerFactory;
    private final ScpiItemReader scpiItemReader;
    private final ScpiItemProcessor scpiItemProcessor;
    private final BatchJobListener batchJobListener;
    private final PlatformTransactionManager transactionManager;
    private final DeleteMissingScpiTasklet deleteMissingScpiTasklet;
    private final JobRepository jobRepository;

    private final MongoItemWriter mongoItemWriter;
    private final ElasticItemWriter elasticItemWriter;
    private final BatchService batchService;

    public BatchConfig(
            EntityManagerFactory entityManagerFactory,
            ScpiItemReader scpiItemReader,
            BatchService batchService,
            ScpiItemProcessor scpiItemProcessor,
            BatchJobListener batchJobListener,
            PlatformTransactionManager transactionManager,
            DeleteMissingScpiTasklet deleteMissingScpiTasklet,
            JobRepository jobRepository,
            MongoItemWriter mongoItemWriter, ElasticItemWriter elasticItemWriter) {
        this.entityManagerFactory = entityManagerFactory;
        this.scpiItemReader = scpiItemReader;
        this.batchService = batchService;
        this.scpiItemProcessor = scpiItemProcessor;
        this.batchJobListener = batchJobListener;
        this.transactionManager = transactionManager;
        this.deleteMissingScpiTasklet = deleteMissingScpiTasklet;
        this.jobRepository = jobRepository;
        this.mongoItemWriter = mongoItemWriter;
        this.elasticItemWriter = elasticItemWriter;
    }

    @Bean
    public CompositeItemProcessor<ScpiDto, Scpi> processor() {
        CompositeItemProcessor<ScpiDto, Scpi> compositeProcessor = new CompositeItemProcessor<>();

        ItemProcessor<ScpiDto, Scpi> conversionProcessor = scpiRequest -> {
            BatchDataDto batchData = batchService.convertToBatchData(scpiRequest);
            return scpiItemProcessor.process(batchData);
        };
        compositeProcessor.setDelegates(List.of(new EncodingCorrectionProcessor<>(), conversionProcessor));
        return compositeProcessor;
    }

    @Bean
    public ItemWriter<Scpi> jpaWriter() {
        JpaItemWriter<Scpi> jpaWriter = new JpaItemWriter<>();
        jpaWriter.setEntityManagerFactory(entityManagerFactory);
        return items -> {
            if (!items.isEmpty()) {
                log.info("Insertion/Mise à jour de {} SCPIs.", items.size());
                jpaWriter.write(items);
            } else {
                log.info("Aucune SCPI à traiter.");
            }
        };
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

    @Bean
    public JpaPagingItemReader<Scpi> scpiPostgresReader() {
        JpaPagingItemReader<Scpi> reader = new JpaPagingItemReader<>();
        reader.setEntityManagerFactory(entityManagerFactory);
        reader.setQueryString("SELECT s FROM Scpi s");
        reader.setPageSize(20);
        return reader;
    }

    @Bean
    public Step deleteStep() {
        return new StepBuilder("deleteStep", jobRepository)
                .tasklet(deleteMissingScpiTasklet, transactionManager)
                .build();
    }

    @Bean
    public Step importStep() {
        return new StepBuilder("importStep", jobRepository)
                .<ScpiDto, Scpi>chunk(20, transactionManager)
                .reader(scpiItemReader.reader())
                .processor(processor())
                .writer(jpaWriter())
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(100)
                .build();
    }

    @Bean
    public Step updateMongoElasticStep() {
        return new StepBuilder("updateMongoElasticStep", jobRepository)
                .<Scpi, Scpi>chunk(20, transactionManager)
                .reader(scpiPostgresReader())
                .writer(compositeWriter())
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(100)
                .build();
    }

    @Bean
    public Job importScpiJob() {
        return new JobBuilder("importScpiJob", jobRepository)
                .listener(batchJobListener)
                .incrementer(new RunIdIncrementer())
                .start(deleteStep())
                .start(importStep())
                .next(updateMongoElasticStep())
                .build();
    }
}
