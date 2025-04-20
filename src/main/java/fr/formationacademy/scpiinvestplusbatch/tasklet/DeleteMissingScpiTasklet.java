package fr.formationacademy.scpiinvestplusbatch.tasklet;

import fr.formationacademy.scpiinvestplusbatch.entity.postgres.Scpi;
import fr.formationacademy.scpiinvestplusbatch.repository.postgres.ScpiRepository;
import fr.formationacademy.scpiinvestplusbatch.service.S3FileService;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Component
@Slf4j
public class DeleteMissingScpiTasklet implements Tasklet {

    private final ScpiRepository scpiRepository;
    private final S3FileService s3FileService;

    public DeleteMissingScpiTasklet(ScpiRepository scpiRepository, S3FileService s3FileService) {
        this.scpiRepository = scpiRepository;
        this.s3FileService = s3FileService;
    }

    @Override
    public RepeatStatus execute(@NotNull StepContribution contribution, @NotNull ChunkContext chunkContext) throws Exception {
        Set<String> scpiNamesInCsv = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                s3FileService.getScpiFileAsStream(), StandardCharsets.UTF_8))) {

            reader.lines()
                    .skip(1)
                    .map(line -> line.split(",")[0].trim().toLowerCase())
                    .forEach(scpiNamesInCsv::add);

        } catch (IOException e) {
            log.error("Erreur lors de la lecture du fichier CSV", e);
            throw e;
        }

        log.info("SCPIs présents dans le CSV : {}", scpiNamesInCsv);

        List<Scpi> allScpis = scpiRepository.findAll();
        List<Scpi> scpisToDelete = allScpis.stream()
                .filter(scpi -> !scpiNamesInCsv.contains(scpi.getName().trim().toLowerCase()))
                .toList();

        if (scpisToDelete.isEmpty()) {
            log.info("Aucune SCPI à supprimer.");
        } else {
            scpisToDelete.forEach(scpi -> log.info("Suppression prévue: {}", scpi.getName()));
            scpiRepository.deleteAll(scpisToDelete);
            log.info("{} SCPI(s) supprimée(s) avec succès.", scpisToDelete.size());
        }

        return RepeatStatus.FINISHED;
    }

}
