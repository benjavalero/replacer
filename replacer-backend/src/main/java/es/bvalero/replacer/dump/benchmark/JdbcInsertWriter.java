package es.bvalero.replacer.dump.benchmark;

import es.bvalero.replacer.replacement.ReplacementEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Slf4j
public class JdbcInsertWriter extends JdbcBatchItemWriter<ReplacementEntity> {

    public JdbcInsertWriter(NamedParameterJdbcTemplate jdbcTemplate) {
        setJdbcTemplate(jdbcTemplate);
        setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        setSql("INSERT INTO replacement2 (articleId, type, subtype, position, lastUpdate) "
            + "VALUES (:articleId, :type, :subtype, :position, :lastUpdate)");

        afterPropertiesSet();
    }
}
