package fr.formationacademy.scpiinvestplusbatch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class BatchStepListener implements StepExecutionListener {

    private final Map<String, Instant> startTimes = new HashMap<>();

    @Override
    public void beforeStep(StepExecution stepExecution) {
        String stepName = stepExecution.getStepName();
        startTimes.put(stepName, Instant.now());

        switch (stepName) {
            case "postgresStep":
                log.info("[POSTGRES] Start processing");
                break;
            case "mongoStep":
                log.info("[MONGO] Start writing");
                break;
            case "elasticStep":
                log.info("[ELASTIC] Start writing");
                break;
            case "deleteStep":
                log.info("[DELETE] Start deleting missing SCPIs");
                break;
            default:
                log.info("[{}] Step started", stepName);
        }
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        String stepName = stepExecution.getStepName();
        Instant start = startTimes.get(stepName);
        long duration = (start != null) ? Instant.now().toEpochMilli() - start.toEpochMilli() : -1;

        switch (stepName) {
            case "postgresStep":
                log.info("[POSTGRES] Completed in {} ms", duration);
                break;
            case "mongoStep":
                log.info("[MONGO] Completed in {} ms", duration);
                break;
            case "elasticStep":
                log.info("[ELASTIC] Completed in {} ms", duration);
                break;
            case "deleteStep":
                log.info("[DELETE] Completed");
                break;
            default:
                log.info("[{}] Step completed in {} ms", stepName, duration);
        }

        return ExitStatus.COMPLETED;
    }
}

