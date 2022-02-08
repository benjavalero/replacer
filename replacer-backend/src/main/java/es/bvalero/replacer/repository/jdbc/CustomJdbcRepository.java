package es.bvalero.replacer.repository.jdbc;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaPageId;
import es.bvalero.replacer.repository.CustomModel;
import es.bvalero.replacer.repository.CustomRepository;
import es.bvalero.replacer.repository.ResultCount;
import java.util.Collection;
import java.util.Objects;
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
    public void addCustom(CustomModel entity) {
        final String sql =
            "INSERT INTO custom (article_id, lang, replacement, cs, position, context, reviewer) " +
            "VALUES (:pageId, :lang, :replacement, :cs, :position, :context, :reviewer)";
        SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(entity);
        jdbcTemplate.update(sql, namedParameters);
    }

    @Override
    public void updateReviewerByPageAndType(
        WikipediaPageId wikipediaPageId,
        String replacement,
        boolean cs,
        String reviewer
    ) {
        String from = "UPDATE custom SET reviewer=:reviewer ";
        String where =
            "WHERE lang = :lang AND article_id = :pageId AND replacement = :replacement AND cs = :cs AND reviewer IS NULL ";
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("reviewer", reviewer)
            .addValue("lang", wikipediaPageId.getLang().getCode())
            .addValue("pageId", wikipediaPageId.getPageId())
            .addValue("replacement", replacement)
            .addValue("cs", cs ? 1 : 0);

        String sql = from + where;
        jdbcTemplate.update(sql, namedParameters);
    }

    // Using DISTINCT makes the query not to use to wanted index "idx_count"
    @Override
    public Collection<Integer> findPageIdsReviewed(WikipediaLanguage lang, String replacement, boolean cs) {
        String sql =
            "SELECT article_id FROM custom " +
            "WHERE lang = :lang AND replacement = :replacement AND cs = :cs AND reviewer IS NOT NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("lang", lang.getCode())
            .addValue("replacement", replacement)
            .addValue("cs", cs ? 1 : 0);
        return jdbcTemplate
            .queryForList(sql, namedParameters, Integer.class)
            .stream()
            .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public int countReplacementsReviewed(WikipediaLanguage lang) {
        String sql = "SELECT COUNT(*) FROM replacement WHERE lang = :lang AND reviewer IS NOT NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("lang", lang.getCode());
        Integer result = jdbcTemplate.queryForObject(sql, namedParameters, Integer.class);
        return Objects.requireNonNullElse(result, 0);
    }

    @Override
    public Collection<ResultCount<String>> countReplacementsByReviewer(WikipediaLanguage lang) {
        String sql =
            "SELECT reviewer, COUNT(*) AS num FROM replacement " +
            "WHERE lang = :lang AND reviewer IS NOT NULL " +
            "GROUP BY reviewer";
        SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("lang", lang.getCode());
        return Objects.requireNonNull(
            jdbcTemplate.query(
                sql,
                namedParameters,
                (resultSet, rowNum) -> ResultCount.of(resultSet.getString("REVIEWER"), resultSet.getInt("NUM"))
            )
        );
    }
}
