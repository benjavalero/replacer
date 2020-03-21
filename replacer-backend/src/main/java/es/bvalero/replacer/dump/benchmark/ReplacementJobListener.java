package es.bvalero.replacer.dump.benchmark;

import es.bvalero.replacer.replacement.ReplacementRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ReplacementJobListener extends JobExecutionListenerSupport {
    @Autowired
    ReplacementRepository replacementRepository;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        /**
         * As of now empty but can add some before job conditions
         */
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            long count = replacementRepository.count();
            LOGGER.info("BATCH JOB COMPLETED SUCCESSFULLY: {}", count);
        }
    }
}
