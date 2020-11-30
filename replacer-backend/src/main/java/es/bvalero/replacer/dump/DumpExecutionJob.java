package es.bvalero.replacer.dump;

import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.replacement.ReplacementEntity;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import javax.annotation.PostConstruct;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.item.xml.builder.StaxEventItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;

@Component
public class DumpExecutionJob {
    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Value("${replacer.dump.batch.chunk.size}")
    private int chunkSize;

    @Autowired
    private DumpPageProcessor dumpPageProcessor;

    @Autowired
    private DumpWriter dumpWriter;

    @Autowired
    private DumpJobListener dumpJobListener;

    @Bean
    public Job dumpJob(ItemReader<DumpPageXml> dumpReader) {
        Step step = stepBuilderFactory
            .get(DumpManager.PARSE_XML_STEP_NAME)
            .<DumpPageXml, List<ReplacementEntity>>chunk(chunkSize)
            .reader(dumpReader)
            .processor(dumpPageProcessor)
            .writer(dumpWriter)
            .faultTolerant()
            .skipLimit(Integer.MAX_VALUE) // No skip limit
            .skip(ReplacerException.class)
            .processorNonTransactional()
            .build();

        return jobBuilderFactory
            .get(DumpManager.DUMP_JOB_NAME)
            .incrementer(new RunIdIncrementer())
            .listener(dumpJobListener)
            .start(step)
            .build();
    }

    @Bean
    @StepScope
    public StaxEventItemReader<DumpPageXml> dumpReader(@Value("#{jobParameters[dumpPath]}") String dumpPath)
        throws IOException {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(DumpPageXml.class);

        Resource resource = new InputStreamResource(
            new BZip2CompressorInputStream(Files.newInputStream(Paths.get(dumpPath)), true)
        );

        return new StaxEventItemReaderBuilder<DumpPageXml>()
            .name("dump-reader")
            .resource(resource)
            .unmarshaller(marshaller)
            .addFragmentRootElements("{http://www.mediawiki.org/xml/export-0.10/}page")
            .build();
    }

    @PostConstruct
    public void setProperty() {
        System.setProperty("jdk.xml.totalEntitySizeLimit", String.valueOf(Integer.MAX_VALUE));
    }
}
