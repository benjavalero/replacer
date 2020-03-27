package es.bvalero.replacer.dump;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST actions to start the dump processing or find the current status.
 */
@Slf4j
@RestController
@RequestMapping("api/dump")
public class DumpController {
    @Autowired
    private DumpManager dumpManager;

    @Autowired
    private JobExplorer jobExplorer;

    @Autowired
    private JobOperator jobOperator;

    @GetMapping(value = "")
    public DumpIndexation getDumpStatus() {
        return getDumpIndexation();
    }

    @PostMapping(value = "")
    public void processLatestDumpFileManually() {
        dumpManager.processLatestDumpFile();
    }

    private DumpIndexation getDumpIndexation() {
        DumpIndexation dumpIndexation = new DumpIndexation();

        // Find job execution
        JobInstance jobInstance = jobExplorer.getLastJobInstance(DumpManager.DUMP_JOB_NAME);
        if (jobInstance != null) {
            JobExecution jobExecution = jobExplorer.getLastJobExecution(jobInstance);
            if (jobExecution != null) {
                dumpIndexation.setRunning(jobExecution.isRunning());
                dumpIndexation.setStart(jobExecution.getStartTime().getTime());
                if (!jobExecution.isRunning()) {
                    dumpIndexation.setEnd(jobExecution.getEndTime().getTime());
                }
                dumpIndexation.setDumpFileName(
                    jobExecution.getJobParameters().getString(DumpManager.DUMP_PATH_PARAMETER)
                );

                // Find step executions
                try {
                    Map<Long, String> map = jobOperator.getStepExecutionSummaries(jobExecution.getId());
                    if (!map.isEmpty()) {
                        long stepId = map.keySet().stream().findAny().orElse(0L);
                        StepExecution stepExecution = jobExplorer.getStepExecution(jobExecution.getId(), stepId);
                        if (stepExecution != null) {
                            dumpIndexation.setNumArticlesRead(stepExecution.getReadCount());
                            dumpIndexation.setNumArticlesProcessed(stepExecution.getWriteCount());
                        }
                    }
                } catch (NoSuchJobExecutionException e) {
                    LOGGER.error("Error finding step executions", e);
                }
            }
        }

        return dumpIndexation;
    }
}
