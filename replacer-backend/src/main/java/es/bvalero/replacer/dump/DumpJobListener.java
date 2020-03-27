package es.bvalero.replacer.dump;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DumpJobListener extends JobExecutionListenerSupport {
    @Autowired
    private ReplacementCache replacementCache;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        // Do nothing
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        replacementCache.clean();
    }
}
