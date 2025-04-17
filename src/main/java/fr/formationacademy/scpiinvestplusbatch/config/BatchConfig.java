package fr.formationacademy.scpiinvestplusbatch.config;

import java.net.SocketTimeoutException;

import fr.formationacademy.scpiinvestplusbatch.dto.ScpiDto;
import fr.formationacademy.scpiinvestplusbatch.entity.elastic.ScpiDocument;
import fr.formationacademy.scpiinvestplusbatch.entity.mongo.ScpiMongo;
import fr.formationacademy.scpiinvestplusbatch.entity.postgres.Scpi;
import fr.formationacademy.scpiinvestplusbatch.listener.BatchJobListener;
import fr.formationacademy.scpiinvestplusbatch.listener.BatchStepListener;
import fr.formationacademy.scpiinvestplusbatch.processor.ScpiToElasticProcessor;
import fr.formationacademy.scpiinvestplusbatch.processor.ScpiToMongoProcessor;
import fr.formationacademy.scpiinvestplusbatch.reader.ScpiItemReader;
import fr.formationacademy.scpiinvestplusbatch.tasklet.DeleteMissingScpiTasklet;
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
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.HttpServerErrorException;

@Configuration
@EnableBatchProcessing
@EnableScheduling
@Slf4j
public class BatchConfig {

    private final ScpiItemReader scpiItemReader;
    private final CompositeItemProcessor<ScpiDto, Scpi> compositeItemProcessor;
    private final ScpiToElasticProcessor scpiToElasticProcessor;
    private final ScpiToMongoProcessor scpiToMongoProcessor;
    private final BatchJobListener batchJobListener;
    private final BatchStepListener batchStepListener;
    private final PlatformTransactionManager transactionManager;
    private final DeleteMissingScpiTasklet deleteMissingScpiTasklet;
    private final JobRepository jobRepository;
    private final PostgresItemWriter postgresItemWriter;
    private final MongoItemWriter mongoItemWriter;
    private final ElasticItemWriter elasticItemWriter;

    public BatchConfig(
            ScpiItemReader scpiItemReader, CompositeItemProcessor<ScpiDto, Scpi> compositeItemProcessor, ScpiToMongoProcessor scpiToMongoProcessor, ScpiToElasticProcessor scpiToElasticProcessor,
            BatchJobListener batchJobListener, BatchStepListener batchStepListener,
            PlatformTransactionManager transactionManager,
            DeleteMissingScpiTasklet deleteMissingScpiTasklet,
            JobRepository jobRepository,
            PostgresItemWriter postgresItemWriter,
            MongoItemWriter mongoItemWriter,
            ElasticItemWriter elasticItemWriter
    ) {
        this.scpiItemReader = scpiItemReader;
        this.compositeItemProcessor = compositeItemProcessor;
        this.scpiToElasticProcessor = scpiToElasticProcessor;
        this.scpiToMongoProcessor = scpiToMongoProcessor;
        this.batchJobListener = batchJobListener;
        this.batchStepListener = batchStepListener;
        this.transactionManager = transactionManager;
        this.deleteMissingScpiTasklet = deleteMissingScpiTasklet;
        this.jobRepository = jobRepository;
        this.postgresItemWriter = postgresItemWriter;
        this.mongoItemWriter = mongoItemWriter;
        this.elasticItemWriter = elasticItemWriter;
    }

    @Bean
    public Step postgresStep() {
        return new StepBuilder("postgresStep", jobRepository)
                .<ScpiDto, Scpi>chunk(20, transactionManager)
                .reader(scpiItemReader.reader())
                .processor(compositeItemProcessor)
                .writer(postgresItemWriter)
                .listener(batchStepListener)
                .faultTolerant()
                .skipLimit(50)
                .skip(Exception.class)
                .noSkip(FlatFileParseException.class)
                .retry(TransientDataAccessException.class)
                .retry(SocketTimeoutException.class)
                .retry(HttpServerErrorException.class)
                .retryLimit(3)
                .build();
    }

    @Bean
    public Step mongoStep() {
        return new StepBuilder("mongoStep", jobRepository)
                .<Scpi, ScpiMongo>chunk(20, transactionManager)
                .reader(scpiItemReader.scpiPostgresReader())
                .processor(scpiToMongoProcessor)
                .writer(mongoItemWriter)
                .listener(batchStepListener)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(50)
                .retry(TransientDataAccessException.class)
                .retry(SocketTimeoutException.class)
                .retry(HttpServerErrorException.class)
                .retryLimit(3)
                .build();
    }

    @Bean
    public Step elasticStep() {
        return new StepBuilder("elasticStep", jobRepository)
                .<Scpi, ScpiDocument>chunk(20, transactionManager)
                .reader(scpiItemReader.scpiPostgresReader())
                .processor(scpiToElasticProcessor)
                .writer(items -> {
                    log.info("[ELASTIC] Writing {} items", items.size());
                    elasticItemWriter.write(items);
                })
                .listener(batchStepListener)
                .listener(batchStepListener)
                .faultTolerant()
                .skip(Exception.class)
                .retry(TransientDataAccessException.class)
                .retry(SocketTimeoutException.class)
                .retry(HttpServerErrorException.class)
                .skipLimit(50)
                .retryLimit(3)
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
    public Step deleteStep() {
        return new StepBuilder("deleteStep", jobRepository)
                .tasklet(deleteMissingScpiTasklet, transactionManager)
                .listener(batchStepListener)
                .build();
    }

    @Bean
    public Step errorStep() {
        return new StepBuilder("errorStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.error("[ERROR] Postgres step failed. Redirecting to error step.");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Flow parallelSteps(TaskExecutor taskExecutor) {
        Flow mongoFlow = new FlowBuilder<Flow>("mongoFlow")
                .start(mongoStep())
                .build();

        Flow elasticFlow = new FlowBuilder<Flow>("elasticFlow")
                .start(elasticStep())
                .build();

        return new FlowBuilder<Flow>("parallelFlow")
                .split(taskExecutor)
                .add(mongoFlow, elasticFlow)
                .build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("BatchExecutor-");
        executor.initialize();
        return executor;
    }

    @Bean
    @Primary
    public Job insertScpiJob(TaskExecutor taskExecutor) {
        return new JobBuilder("insertScpiJob", jobRepository)
                .listener(batchJobListener)
                .incrementer(new RunIdIncrementer())
                .start(postgresStep())
                .on("FAILED").to(errorStep())
                .from(postgresStep()).on("COMPLETED").to(parallelSteps(taskExecutor))
                .end()
                .build();
    }

    @Bean
    public Job deleteJob() {
        return new JobBuilder("deleteJob", jobRepository)
                .listener(batchJobListener)
                .incrementer(new RunIdIncrementer())
                .start(deleteStep())
                .build();
    }
}
