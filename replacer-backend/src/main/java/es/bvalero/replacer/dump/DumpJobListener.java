package es.bvalero.replacer.dump;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DumpJobListener implements JobExecutionListener {
    @Autowired
    private ReplacementCache replacementCache;

    @Override
    public void beforeJob(@NotNull JobExecution jobExecution) {
        LOGGER.debug("START Dump Job Execution");
    }

    @Override
    public void afterJob(@NotNull JobExecution jobExecution) {
        LOGGER.debug("END Dump Job Execution");
        replacementCache.finish();
    }
}