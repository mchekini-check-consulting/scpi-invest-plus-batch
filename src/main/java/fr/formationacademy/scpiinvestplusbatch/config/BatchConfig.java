package fr.formationacademy.scpiinvestplusbatch.config;

import fr.formationacademy.scpiinvestplusbatch.dto.BatchDataDto;
import fr.formationacademy.scpiinvestplusbatch.dto.ScpiDto;
import fr.formationacademy.scpiinvestplusbatch.entity.postgrs.Scpi;
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
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableBatchProcessing
@EnableScheduling
@Slf4j
public class BatchConfig {

    private final EntityManagerFactory entityManagerFactory;
    private final ScpiItemReader scpiItemReader;
    private final BatchService batchService;
    private final ScpiItemProcessor scpiItemProcessor;
    private final BatchJobListener batchJobListener;
    private final PlatformTransactionManager transactionManager;
    private final DeleteMissingScpiTasklet deleteMissingScpiTasklet;
    private final JobRepository jobRepository;
    private final MongoItemWriter mongoItemWriter;
    private final ElasticItemWriter elasticItemWriter;

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
        CompositeItemWriter<Scpi> compositeItemWriter = new CompositeItemWriter<>();
        List<ItemWriter<? super Scpi>> writers = new ArrayList<>();
        writers.add(mongoItemWriter);
        writers.add(elasticItemWriter);

        compositeItemWriter.setDelegates(writers);
        return compositeItemWriter;
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
