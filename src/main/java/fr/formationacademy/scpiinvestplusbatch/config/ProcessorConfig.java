package fr.formationacademy.scpiinvestplusbatch.config;

import fr.formationacademy.scpiinvestplusbatch.dto.ScpiDto;
import fr.formationacademy.scpiinvestplusbatch.entity.postgres.Scpi;
import fr.formationacademy.scpiinvestplusbatch.processor.ScpiEncodingCorrectionProcessor;
import fr.formationacademy.scpiinvestplusbatch.processor.ScpiToPostgresProcessor;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ProcessorConfig {

    private final ScpiToPostgresProcessor scpiToPostgresProcessor;


    public ProcessorConfig(ScpiToPostgresProcessor scpiToPostgresProcessor) {
        this.scpiToPostgresProcessor = scpiToPostgresProcessor;
    }

    @Bean
    public CompositeItemProcessor<ScpiDto, Scpi> scpiCompositeProcessor() {
        CompositeItemProcessor<ScpiDto, Scpi> compositeProcessor = new CompositeItemProcessor<>();
        compositeProcessor.setDelegates(List.of(new ScpiEncodingCorrectionProcessor<>(), scpiToPostgresProcessor));
        return compositeProcessor;
    }
}
