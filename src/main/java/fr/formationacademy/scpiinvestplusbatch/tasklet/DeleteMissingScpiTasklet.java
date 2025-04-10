package fr.formationacademy.scpiinvestplusbatch.tasklet;

import fr.formationacademy.scpiinvestplusbatch.entity.postgres.Scpi;
import fr.formationacademy.scpiinvestplusbatch.repository.postgres.ScpiRepository;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class DeleteMissingScpiTasklet implements Tasklet {

    private final ScpiRepository scpiRepository;

    public DeleteMissingScpiTasklet(ScpiRepository scpiRepository) {
        this.scpiRepository = scpiRepository;
    }

    @Override
    public RepeatStatus execute(@NotNull StepContribution contribution, ChunkContext chunkContext) throws Exception {
        Set<String> scpiNamesInCsv = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource("scpi.csv").getInputStream(), StandardCharsets.UTF_8))) {
            reader.lines()
                    .skip(1)
                    .map(line -> line.split(",")[0])
                    .forEach(scpiNamesInCsv::add);
        }

        List<Scpi> scpisToDelete = scpiRepository.findAll().stream()
                .filter(scpi -> !scpiNamesInCsv.contains(scpi.getName()))
                .toList();

        if (!scpisToDelete.isEmpty()) {
            scpiRepository.deleteAll(scpisToDelete);
            log.info("{} SCPIs supprimées.", scpisToDelete.size());
        }

        return RepeatStatus.FINISHED;
    }
}
