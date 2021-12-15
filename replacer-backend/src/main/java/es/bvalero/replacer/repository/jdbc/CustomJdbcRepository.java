package es.bvalero.replacer.repository.jdbc;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.repository.CustomModel;
import es.bvalero.replacer.repository.CustomRepository;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Loggable(value = LogLevel.TRACE, skipResult = true, warnOver = 10, warnUnit = TimeUnit.SECONDS)
@Repository
@Transactional
class CustomJdbcRepository implements CustomRepository {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public void addCustom(CustomModel entity) {
        final String sql =
            "INSERT INTO custom (article_id, lang, replacement, cs, last_update, reviewer) " +
            "VALUES (:pageId, :lang, :replacement, :cs, :lastUpdate, :reviewer)";
        SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(entity);
        jdbcTemplate.update(sql, namedParameters);
    }

    // Using DISTINCT makes the query not to use to wanted index "idx_count"
    @Override
    public Collection<Integer> findPageIdsReviewed(WikipediaLanguage lang, String replacement, boolean cs) {
        String sql = "SELECT article_id FROM custom WHERE lang = :lang AND replacement = :replacement AND cs = :cs";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("lang", lang.getCode())
            .addValue("replacement", replacement)
            .addValue("cs", cs ? 1 : 0);
        return jdbcTemplate
            .queryForList(sql, namedParameters, Integer.class)
            .stream()
            .collect(Collectors.toUnmodifiableSet());
    }
}
