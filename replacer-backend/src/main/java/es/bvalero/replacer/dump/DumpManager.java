package es.bvalero.replacer.dump;

import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.nio.file.Path;
import java.util.Map;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Find the Wikipedia dumps in the filesystem where the application runs.
 * This indexing will be done periodically, or manually from @{@link DumpController}.
 * The dumps are parsed with @{@link DumpExecutionJob}.
 * Each page found in the dump is processed in @{@link DumpPageProcessor}.
 */
@Slf4j
@Component
class DumpManager {
    static final String DUMP_JOB_NAME = "dump-job";
    static final String DUMP_PATH_PARAMETER = "dumpPath";
    static final String DUMP_LANG_PARAMETER = "dumpLang";
    static final String PARSE_XML_STEP_NAME = "parse-xml";

    @Autowired
    private DumpFinder dumpFinder;

    @Autowired
    private JobExplorer jobExplorer;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private JobOperator jobOperator;

    @Autowired
    private Job dumpJob;

    @Resource
    private Map<String, Integer> numArticlesEstimated;

    /**
     * Check if there is a new dump to process.
     */
    @Scheduled(
        initialDelayString = "${replacer.dump.batch.delay.initial}",
        fixedDelayString = "${replacer.dump.batch.delay}"
    )
    public void processDumpScheduled() {
        LOGGER.info("EXECUTE Scheduled index of the last dump");
        processLatestDumpFile();
    }

    /**
     * Find the latest dump file and process it
     */
    // In order to be asynchronous it must be public and called externally:
    // https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/scheduling/annotation/EnableAsync.html
    @Async
    public void processLatestDumpFile() {
        LOGGER.info("START Index latest dump file");

        // Check just in case the handler is already running
        if (!jobExplorer.findRunningJobExecutions(DUMP_JOB_NAME).isEmpty()) {
            LOGGER.info("END Index latest dump file. Dump indexing is already running.");
            return;
        }

        for (WikipediaLanguage lang : WikipediaLanguage.values()) {
            try {
                Path latestDumpFileFound = dumpFinder.findLatestDumpFile(lang);
                parseDumpFile(latestDumpFileFound, lang);
                LOGGER.info("END Index latest dump file: {}", latestDumpFileFound);
            } catch (ReplacerException e) {
                LOGGER.error("Error indexing latest dump file", e);
            }
        }
    }

    void parseDumpFile(Path dumpFile, WikipediaLanguage lang) throws ReplacerException {
        LOGGER.info("START Parse dump file: {}", dumpFile);

        try {
            JobParameters jobParameters = new JobParametersBuilder()
                .addString("source", "Dump Manager")
                .addLong("time", System.currentTimeMillis()) // In order to run the job several times
                .addString(DUMP_LANG_PARAMETER, lang.getCode())
                .addString(DUMP_PATH_PARAMETER, dumpFile.toString())
                .toJobParameters();
            jobLauncher.run(dumpJob, jobParameters);
        } catch (
            JobExecutionAlreadyRunningException
            | JobRestartException
            | JobInstanceAlreadyCompleteException
            | JobParametersInvalidException e
        ) {
            throw new ReplacerException("Error running dump batch", e);
        }
    }

    DumpIndexingStatus getDumpIndexingStatus() {
        DumpIndexingStatus dumpIndexingStatus = new DumpIndexingStatus();

        // Find job execution
        JobInstance jobInstance = jobExplorer.getLastJobInstance(DumpManager.DUMP_JOB_NAME);
        if (jobInstance != null) {
            JobExecution jobExecution = jobExplorer.getLastJobExecution(jobInstance);
            if (jobExecution != null) {
                dumpIndexingStatus.setRunning(jobExecution.isRunning());
                dumpIndexingStatus.setStart(jobExecution.getStartTime().getTime());
                if (!jobExecution.isRunning()) {
                    dumpIndexingStatus.setEnd(jobExecution.getEndTime().getTime());
                }
                dumpIndexingStatus.setDumpFileName(
                    jobExecution.getJobParameters().getString(DumpManager.DUMP_PATH_PARAMETER)
                );
                String lang = jobExecution.getJobParameters().getString(DumpManager.DUMP_LANG_PARAMETER);
                // TODO: The not-null check is only needed the first time we index with this new parameter
                if (lang == null) {
                    lang = WikipediaLanguage.SPANISH.getCode();
                }
                dumpIndexingStatus.setNumArticlesEstimated(numArticlesEstimated.get(lang));

                addStepExecutions(dumpIndexingStatus, jobExecution);
            }
        }

        return dumpIndexingStatus;
    }

    private void addStepExecutions(DumpIndexingStatus dumpIndexingStatus, JobExecution jobExecution) {
        try {
            Map<Long, String> map = jobOperator.getStepExecutionSummaries(jobExecution.getId());
            if (!map.isEmpty()) {
                long stepId = map.keySet().stream().findAny().orElse(0L);
                StepExecution stepExecution = jobExplorer.getStepExecution(jobExecution.getId(), stepId);
                if (stepExecution != null) {
                    dumpIndexingStatus.setNumArticlesRead(stepExecution.getReadCount());
                    dumpIndexingStatus.setNumArticlesProcessed(stepExecution.getWriteCount());
                }
            }
        } catch (NoSuchJobExecutionException e) {
            LOGGER.error("Error finding step executions", e);
        }
    }
}
