package fr.formationacademy.scpiinvestplusbatch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class BatchJobListener implements JobExecutionListener {

    private final Map<Long, Long> jobStartTimes = new ConcurrentHashMap<>();

    @Override
    public void beforeJob(JobExecution jobExecution) {
        long startTime = System.currentTimeMillis();
        jobStartTimes.put(jobExecution.getId(), startTime);

        log.info("Batch job '{}' started at {} with parameters: {}",
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getStartTime(),
                jobExecution.getJobParameters());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        long endTime = System.currentTimeMillis();
        Long startTime = jobStartTimes.remove(jobExecution.getId());

        if (startTime != null) {
            long duration = endTime - startTime;
            log.info("Batch job '{}' finished with status: {} (Duration: {} ms)",
                    jobExecution.getJobInstance().getJobName(),
                    jobExecution.getStatus(),
                    duration);
        } else {
            log.debug("Batch job '{}' finished, but start time was not recorded!",
                    jobExecution.getJobInstance().getJobName());
        }

        if (jobExecution.getStatus() == BatchStatus.FAILED) {
            log.error("Batch job '{}' failed! Errors:", jobExecution.getJobInstance().getJobName());
            jobExecution.getAllFailureExceptions().forEach(ex -> log.error("Exception: ", ex));
        }
    }
}
