package es.bvalero.replacer.dump.benchmark;

import es.bvalero.replacer.replacement.ReplacementEntity;
import es.bvalero.replacer.replacement.ReplacementRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ReplacementExecutionJob {
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
    ReplacementRepository replacementRepository;

    @Autowired
    ReplacementJobListener replacementJobListener;

    @Bean
    public Job replacementJob() {
        Step step = stepBuilderFactory
            .get("step-1")
            .<ReplacementEntity, ReplacementEntity>chunk(chunkSize)
            .reader(new CsvReader(resource))
            .processor(replacementProcessor)
            .writer(new RepositoryInsertWriter(replacementRepository))
            .build();

        Step step2 = stepBuilderFactory
            .get("step-2")
            .<ReplacementEntity, ReplacementEntity>chunk(chunkSize)
            .reader(new CsvReader(resource))
            .processor(replacementProcessor)
            .writer(new RepositoryUpdateWriter(replacementRepository))
            .build();

        return jobBuilderFactory
            .get("replacement-job")
            .incrementer(new RunIdIncrementer())
            .listener(replacementJobListener)
            .start(step)
            .next(step2)
            .build();
    }
}
