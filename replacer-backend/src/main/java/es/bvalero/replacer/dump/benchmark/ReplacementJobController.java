package es.bvalero.replacer.dump.benchmark;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReplacementJobController {
    @Autowired
    JobLauncher jobLauncher;

    @Autowired
    Job replacementJob;

    @GetMapping(value = "/run-batch-job")
    public String handle()
        throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        JobParameters jobParameters = new JobParametersBuilder().addString("source", "Spring Boot").toJobParameters();
        jobLauncher.run(replacementJob, jobParameters);

        return "Batch job has been invoked";
    }
}
