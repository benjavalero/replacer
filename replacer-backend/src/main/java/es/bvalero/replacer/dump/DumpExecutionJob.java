package es.bvalero.replacer.dump;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
public class DumpExecutionJob {
    @Autowired
    JobBuilderFactory jobBuilderFactory;

    @Autowired
    StepBuilderFactory stepBuilderFactory;

    @Value("${chunk.size}")
    int chunkSize;

    @Autowired
    DumpProcessor dumpProcessor;

    @Bean
    public Job dumpJob() {
        Path dumpPath = Paths.get("/Users/benja/Developer/eswiki/20200301/eswiki-20200301-pages-articles.xml.bz2");

        Step step = stepBuilderFactory
            .get("step-1")
            .<DumpPage, DumpPage>chunk(chunkSize)
            .reader(new DumpReader(dumpPath))
            // .processor(dumpProcessor)
            .writer(new DumpWriter())
            .build();

        return jobBuilderFactory
            .get("dump-job")
            .incrementer(new RunIdIncrementer())
            .start(step)
            .build();
    }
}
