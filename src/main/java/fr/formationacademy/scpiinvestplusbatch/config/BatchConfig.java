package fr.formationacademy.scpiinvestplusbatch.config;

import fr.formationacademy.scpiinvestplusbatch.dto.ScpiDto;
import fr.formationacademy.scpiinvestplusbatch.entity.postgres.Scpi;
import fr.formationacademy.scpiinvestplusbatch.listener.BatchJobListener;
import fr.formationacademy.scpiinvestplusbatch.processor.ScpiItemProcessor;
import fr.formationacademy.scpiinvestplusbatch.reader.ScpiItemReader;
import fr.formationacademy.scpiinvestplusbatch.tasklet.DeleteMissingScpiTasklet;
import fr.formationacademy.scpiinvestplusbatch.writer.PostgresItemWriter;
import fr.formationacademy.scpiinvestplusbatch.writer.shared.CompositeWriter;
import jakarta.persistence.EntityManagerFactory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;

import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;

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

    private final PostgresItemWriter postgresItemWriter;
    private final CompositeWriter compositeWriter;

    public BatchConfig(
            EntityManagerFactory entityManagerFactory,
            ScpiItemReader scpiItemReader,
            ScpiItemProcessor scpiItemProcessor,
            BatchJobListener batchJobListener,
            PlatformTransactionManager transactionManager,
            DeleteMissingScpiTasklet deleteMissingScpiTasklet,
            JobRepository jobRepository, PostgresItemWriter postgresItemWriter,
            CompositeWriter compositeWriter)
    {
        this.entityManagerFactory = entityManagerFactory;
        this.scpiItemReader = scpiItemReader;
        this.scpiItemProcessor = scpiItemProcessor;
        this.batchJobListener = batchJobListener;
        this.transactionManager = transactionManager;
        this.deleteMissingScpiTasklet = deleteMissingScpiTasklet;
        this.jobRepository = jobRepository;
        this.postgresItemWriter = postgresItemWriter;
        this.compositeWriter = compositeWriter;

    }

    @Bean
    public Step postgresStep() {
        return new StepBuilder("postgresStep", jobRepository)
                .<ScpiDto, Scpi>chunk(20, transactionManager)
                .reader(scpiItemReader.reader())
                .processor(scpiItemProcessor.processor())
                .writer(postgresItemWriter)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(52)
                .build();

    }

    @Bean
    public Step mongoElasticStep() {
        return new StepBuilder("mongoElasticStep", jobRepository)
                .<Scpi, Scpi>chunk(20, transactionManager)
                .reader(scpiItemReader.scpiPostgresReader())
                .writer(compositeWriter.compositeWriter())
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(100)
                .build();

    }

    @Bean
    public Step deleteStep() {
        return new StepBuilder("deleteStep", jobRepository)
                .tasklet(deleteMissingScpiTasklet, transactionManager)
                .build();
    }

    @Bean
    public Job scpiJob() {

        return new JobBuilder("scpiJob", jobRepository)
                .listener(batchJobListener)
                .incrementer(new RunIdIncrementer())
                .start(deleteStep())
                .start(postgresStep())
                .next(mongoElasticStep())
                .build();
    }
}
