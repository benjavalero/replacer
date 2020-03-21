package es.bvalero.replacer.dump.benchmark;

import es.bvalero.replacer.replacement.ReplacementEntity;
import es.bvalero.replacer.replacement.ReplacementRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ReplacementExecutionJob extends JobExecutionListenerSupport {

    @Autowired
    JobBuilderFactory jobBuilderFactory;

    @Autowired
    StepBuilderFactory stepBuilderFactory;

    @Value("${input.file}")
    Resource resource;

    @Value("${chunk.size}")
    int chunkSize;

    @Autowired
    ReplacementProcessor replacementProcessor;

    @Autowired
    JpaInsertWriter jpaInsertWriter;

    @Autowired
    ReplacementRepository replacementRepository;

    @Bean
    public Job replacementJob() {

        Step step = stepBuilderFactory.get("step-1")
            .<ReplacementEntity, ReplacementEntity>chunk(chunkSize)
            .reader(new CsvReader(resource))
            .processor(replacementProcessor)
            .writer(jpaInsertWriter)
            .build();

        /*
        Step step2 = stepBuilderFactory.get("step-2")
            .<ReplacementEntity, ReplacementEntity>chunk(chunkSize / 5)
            .reader(new CsvReader(resourceUpdate))
            .processor(replacementProcessor)
            .writer(jpaWriter)
            .build();
         */

        return jobBuilderFactory.get("replacement-job")
            .incrementer(new RunIdIncrementer())
            .listener(this)
            .start(step)
            // .next(step2)
            .build();
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            long count = replacementRepository.count();
            LOGGER.info("BATCH JOB COMPLETED SUCCESSFULLY: {}", count);
        }
    }

}
