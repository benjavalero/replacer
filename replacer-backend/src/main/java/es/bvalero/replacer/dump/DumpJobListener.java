package es.bvalero.replacer.dump;

import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import lombok.extern.slf4j.Slf4j;
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
    public void beforeJob(JobExecution jobExecution) {
        LOGGER.trace("START Dump Job Execution");
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        LOGGER.trace("END Dump Job Execution");
        WikipediaLanguage lang = WikipediaLanguage.forValues(
            jobExecution.getJobParameters().getString(DumpManager.DUMP_LANG_PARAMETER)
        );
        replacementCache.finish(lang);
    }
}
