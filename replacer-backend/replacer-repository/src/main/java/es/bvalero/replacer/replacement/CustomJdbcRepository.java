package es.bvalero.replacer.replacement;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.page.PageKey;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Loggable(value = LogLevel.TRACE, skipResult = true, warnOver = 10, warnUnit = TimeUnit.SECONDS)
@Transactional
@Repository
class CustomJdbcRepository implements CustomRepository {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public void add(IndexedCustomReplacement customReplacement) {
        final String sql =
            "INSERT INTO custom (lang, page_id, replacement, cs, start, reviewer) " +
            "VALUES (:pageKey.lang.code, :pageKey.pageId, :replacement, :cs, :start, :reviewer)";
        SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(customReplacement);
        jdbcTemplate.update(sql, namedParameters);
    }

    // Using DISTINCT makes the query not to use to wanted index "idx_count"
    @Override
    public Collection<PageKey> findPagesReviewed(WikipediaLanguage lang, String replacement, boolean caseSensitive) {
        String sql =
            "SELECT page_id FROM custom " +
            "WHERE lang = :lang AND replacement = :replacement AND cs = :cs AND reviewer IS NOT NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("lang", lang.getCode())
            .addValue("replacement", replacement)
            .addValue("cs", IndexedCustomReplacement.getCs(caseSensitive));
        return jdbcTemplate
            .queryForList(sql, namedParameters, Integer.class)
            .stream()
            .map(pageId -> PageKey.of(lang, pageId))
            .collect(Collectors.toUnmodifiableSet());
    }
}
