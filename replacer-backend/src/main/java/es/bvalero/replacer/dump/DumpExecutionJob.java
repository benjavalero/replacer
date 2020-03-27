package es.bvalero.replacer.dump;

import es.bvalero.replacer.replacement.ReplacementEntity;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.item.xml.builder.StaxEventItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
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
    private DumpArticleProcessor dumpArticleProcessor;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private DumpJobListener dumpJobListener;

    @Bean
    public Job dumpJob(
        ItemReader<DumpPage> dumpReader,
        ItemWriter<ReplacementEntity> jdbcInsertWriter,
        ItemWriter<ReplacementEntity> jdbcUpdateWriter
    ) {
        Step step = stepBuilderFactory
            .get(DumpManager.PARSE_XML_STEP_NAME)
            .<DumpPage, List<ReplacementEntity>>chunk(chunkSize)
            .reader(dumpReader)
            .processor(dumpArticleProcessor)
            .writer(new DumpWriter(jdbcInsertWriter, jdbcUpdateWriter))
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
    public StaxEventItemReader<DumpPage> dumpReader(@Value("#{jobParameters[dumpPath]}") String dumpPath)
        throws IOException {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(DumpPage.class);

        Resource resource = new InputStreamResource(
            new BZip2CompressorInputStream(Files.newInputStream(Paths.get(dumpPath)), true)
        );

        return new StaxEventItemReaderBuilder<DumpPage>()
            .name("dump-reader")
            .resource(resource)
            .unmarshaller(marshaller)
            .addFragmentRootElements("{http://www.mediawiki.org/xml/export-0.10/}page")
            .build();
    }

    @Bean
    public JdbcBatchItemWriter<ReplacementEntity> jdbcInsertWriter() {
        final String insertSql =
            "INSERT INTO replacement2 (article_id, type, subtype, position, last_update, reviewer) " +
            "VALUES (:articleId, :type, :subtype, :position, :lastUpdate, :reviewer)";

        return new JdbcBatchItemWriterBuilder<ReplacementEntity>()
            .namedParametersJdbcTemplate(jdbcTemplate)
            .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
            .sql(insertSql)
            .build();
    }

    @Bean
    public JdbcBatchItemWriter<ReplacementEntity> jdbcUpdateWriter() {
        final String updateSql = "UPDATE replacement2 SET last_update=:lastUpdate, reviewer=:reviewer WHERE id=:id";

        return new JdbcBatchItemWriterBuilder<ReplacementEntity>()
            .namedParametersJdbcTemplate(jdbcTemplate)
            .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
            .sql(updateSql)
            .build();
    }
}
