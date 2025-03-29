package fr.formationacademy.scpiinvestplusbatch.config;

import fr.formationacademy.scpiinvestplusbatch.dto.BatchDataDto;
import fr.formationacademy.scpiinvestplusbatch.dto.ScpiDto;
import fr.formationacademy.scpiinvestplusbatch.entity.Scpi;
import fr.formationacademy.scpiinvestplusbatch.listener.BatchJobListener;
import fr.formationacademy.scpiinvestplusbatch.processor.EncodingCorrectionProcessor;
import fr.formationacademy.scpiinvestplusbatch.processor.ScpiItemProcessor;
import fr.formationacademy.scpiinvestplusbatch.reader.ScpiItemReader;
import fr.formationacademy.scpiinvestplusbatch.repository.ScpiRepository;
import fr.formationacademy.scpiinvestplusbatch.service.BatchService;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Configuration
@EnableBatchProcessing
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class BatchConfig {

    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private final BatchJobListener batchJobListener;
    private final ScpiItemReader scpiItemReader;
    private final ScpiRepository scpiRepository;
    @Lazy
    private final JobRepository jobRepository;

    @Bean
    public PlatformTransactionManager batchManager(
            @Qualifier("batchManager") PlatformTransactionManager transactionManager) {
        return transactionManager;
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public CompositeItemProcessor<ScpiDto, Scpi> processor(ScpiItemProcessor scpiItemProcessor, BatchService batchService) {
        CompositeItemProcessor<ScpiDto, Scpi> compositeProcessor = new CompositeItemProcessor<>();

        ItemProcessor<ScpiDto, Scpi> conversionProcessor = scpiRequest -> {
            BatchDataDto batchData = batchService.convertToBatchData(scpiRequest);
            return scpiItemProcessor.process(batchData);
        };

        EncodingCorrectionProcessor<ScpiDto> encodingProcessor = new EncodingCorrectionProcessor<>();
        compositeProcessor.setDelegates(Arrays.asList(encodingProcessor, conversionProcessor));

        return compositeProcessor;
    }

    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(
            @Qualifier("batchEntityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return new JpaTransactionManager(Objects.requireNonNull(entityManagerFactory.getObject()));
    }

    @Bean
    public ItemWriter<Scpi> writer() {
        return items -> {
            if (!items.isEmpty()) {
                log.debug("Insertion/Mise à jour de {} SCPIs dans le lot actuel.", items.size());
                JpaItemWriter<Scpi> jpaWriter = new JpaItemWriter<>();
                jpaWriter.setEntityManagerFactory(entityManagerFactory);
                jpaWriter.write(items);
                log.info("Un {} lot des SCPIs a bien été persisté en base.", items.size());
            } else {
                log.info("Aucune SCPI à insérer ou mettre à jour dans ce lot.");
            }
        };
    }

    @Bean
    public Tasklet deleteMissingScpiTasklet() {
        return (contribution, chunkContext) -> {
            Set<String> scpiNamesInCsv = new HashSet<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new ClassPathResource("scpi.csv").getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                boolean firstLine = true;
                while ((line = reader.readLine()) != null) {
                    if (firstLine) {
                        firstLine = false;
                        continue;
                    }
                    String[] fields = line.split(",");
                    if (fields.length > 0) {
                        scpiNamesInCsv.add(fields[0]);
                    }
                }
            }
            List<Scpi> scpisToDelete = scpiRepository.findAll().stream()
                    .filter(scpi -> !scpiNamesInCsv.contains(scpi.getName()))
                    .toList();

            if (!scpisToDelete.isEmpty()) {
                scpiRepository.deleteAll(scpisToDelete);
                log.info("{} SCPI supprimées car absentes du fichier CSV.", scpisToDelete.size());
            }

            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Step deleteStep() {
        return new StepBuilder("deleteStep", jobRepository)
                .tasklet(deleteMissingScpiTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Step importStep(ItemProcessor<ScpiDto, Scpi> processor, ItemWriter<Scpi> writer) {
        return new StepBuilder("importStep", jobRepository)
                .<ScpiDto, Scpi>chunk(20, transactionManager)
                .reader(scpiItemReader.reader())
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(100)
                .build();
    }

    @Bean
    public Job importScpiJob(Step deleteStep, Step importStep) {
        return new JobBuilder("importScpiJob", jobRepository)
                .listener(batchJobListener)
                .incrementer(new RunIdIncrementer())
                .start(deleteStep)
                .next(importStep)
                .build();
    }
}
