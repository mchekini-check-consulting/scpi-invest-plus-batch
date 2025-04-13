package fr.formationacademy.scpiinvestplusbatch.config;

import fr.formationacademy.scpiinvestplusbatch.dto.ScpiDto;
import fr.formationacademy.scpiinvestplusbatch.entity.elastic.ScpiDocument;
import fr.formationacademy.scpiinvestplusbatch.entity.mongo.ScpiMongo;
import fr.formationacademy.scpiinvestplusbatch.entity.postgres.Scpi;
import fr.formationacademy.scpiinvestplusbatch.listener.BatchJobListener;
import fr.formationacademy.scpiinvestplusbatch.processor.ScpiToElasticProcessor;
import fr.formationacademy.scpiinvestplusbatch.processor.ScpiToMongoProcessor;
import fr.formationacademy.scpiinvestplusbatch.reader.ScpiItemReader;
import fr.formationacademy.scpiinvestplusbatch.writer.ElasticItemWriter;
import fr.formationacademy.scpiinvestplusbatch.writer.MongoItemWriter;
import fr.formationacademy.scpiinvestplusbatch.writer.PostgresItemWriter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;

import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;


@Configuration
@EnableBatchProcessing
@EnableScheduling
@Slf4j
public class BatchConfig {

    private final ScpiItemReader scpiItemReader;
    private final CompositeItemProcessor<ScpiDto, Scpi> compositeItemProcessor;
    private final BatchJobListener batchJobListener;
    private final PlatformTransactionManager transactionManager;
    private final JobRepository jobRepository;
    private final ScpiToMongoProcessor scpiToMongoProcessor;
    private final ScpiToElasticProcessor scpiToElasticProcessor;
    private final MongoItemWriter mongoItemWriter;
    private final ElasticItemWriter elasticItemWriter;
    private final PostgresItemWriter postgresItemWriter;

    public BatchConfig(
            ScpiItemReader scpiItemReader,
            CompositeItemProcessor<ScpiDto, Scpi> compositeItemProcessor,
            BatchJobListener batchJobListener,
            PlatformTransactionManager transactionManager,
            JobRepository jobRepository, ScpiToMongoProcessor scpiToMongoProcessor, ScpiToElasticProcessor scpiToElasticProcessor, MongoItemWriter mongoItemWriter, ElasticItemWriter elasticItemWriter, PostgresItemWriter postgresItemWriter)
    {
        this.scpiItemReader = scpiItemReader;
        this.compositeItemProcessor = compositeItemProcessor;
        this.batchJobListener = batchJobListener;
        this.transactionManager = transactionManager;
        this.jobRepository = jobRepository;
        this.scpiToMongoProcessor = scpiToMongoProcessor;
        this.scpiToElasticProcessor = scpiToElasticProcessor;
        this.mongoItemWriter = mongoItemWriter;
        this.elasticItemWriter = elasticItemWriter;
        this.postgresItemWriter = postgresItemWriter;
    }

    @Bean
    public Step importToPostgresStep() {
        return new StepBuilder("postgresStep", jobRepository)
                .<ScpiDto, Scpi>chunk(20, transactionManager)
                .reader(scpiItemReader.reader())
                .processor(compositeItemProcessor)
                .writer(postgresItemWriter)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(52)
                .build();

    }

    @Bean
    public Step importToMongoStep() {
        return new StepBuilder("ImportToMongoStep", jobRepository)
                .<Scpi, ScpiMongo>chunk(20, transactionManager)
                .reader(scpiItemReader.scpiPostgresReader())
                .processor(scpiToMongoProcessor)
                .writer(mongoItemWriter)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(100)
                .build();
    }

    @Bean
    public Step importToElasticStep() {
        return new StepBuilder("ImportToElasticStep", jobRepository)
                .<Scpi, ScpiDocument>chunk(20, transactionManager)
                .reader(scpiItemReader.scpiPostgresReader())
                .processor(scpiToElasticProcessor)
                .writer(elasticItemWriter)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(100)
                .build();

    }

    @Bean
    public Flow postgresFlow() {
        return new FlowBuilder<Flow>("postgresFlow")
                .start(importToPostgresStep())
                .build();
    }

    @Bean
    public Flow mongoFlow() {
        return new FlowBuilder<Flow>("mongoFlow")
                .start(importToMongoStep())
                .build();
    }

    @Bean
    public Flow elasticFlow() {
        return new FlowBuilder<Flow>("elasticFlow")
                .start(importToElasticStep())
                .build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutor("spring_batch_async");
    }

    @Bean
    public Flow parallelFlow() {
        return new FlowBuilder<Flow>("parallelFlow")
                .split(taskExecutor())
                .add(mongoFlow(), elasticFlow())
                .build();
    }

    @Bean
    public Job scpiJob() {
        return new JobBuilder("scpiJob", jobRepository)
                .listener(batchJobListener)
                .incrementer(new RunIdIncrementer())
                .start(postgresFlow())
                .next(parallelFlow())
                .end()
                .build();
    }
}
