package es.bvalero.replacer.dump.benchmark;

import es.bvalero.replacer.replacement.ReplacementEntity;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.core.io.Resource;

public class CsvReader extends FlatFileItemReader<ReplacementEntity> {

    public CsvReader(Resource resource) {
        super();

        setResource(resource);

        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setNames("articleId", "type", "subtype", "position");
        lineTokenizer.setDelimiter("\t");
        lineTokenizer.setStrict(false);

        BeanWrapperFieldSetMapper<ReplacementEntity> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(ReplacementEntity.class);

        DefaultLineMapper<ReplacementEntity> defaultLineMapper = new DefaultLineMapper<>();
        defaultLineMapper.setLineTokenizer(lineTokenizer);
        defaultLineMapper.setFieldSetMapper(fieldSetMapper);
        setLineMapper(defaultLineMapper);
    }
}
