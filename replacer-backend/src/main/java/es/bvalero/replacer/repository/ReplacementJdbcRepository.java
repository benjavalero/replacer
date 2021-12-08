package es.bvalero.replacer.repository;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.logging.LogLevel;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Loggable(value = LogLevel.TRACE, skipResult = true, warnOver = 10, warnUnit = TimeUnit.SECONDS)
@Transactional
@Repository
@Qualifier("replacementJdbcRepository")
class ReplacementJdbcRepository implements ReplacementRepository {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public void addReplacements(Collection<ReplacementModel> replacements) {
        String sql =
            "INSERT INTO replacement (article_id, lang, type, subtype, position, context, last_update, reviewer) " +
            "VALUES (:pageId, :lang, :type, :subtype, :position, :context, :lastUpdate, :reviewer)";
        SqlParameterSource[] namedParameters = SqlParameterSourceUtils.createBatch(replacements.toArray());
        jdbcTemplate.batchUpdate(sql, namedParameters);
    }

    @Override
    public void updateReplacements(Collection<ReplacementModel> replacements) {
        String sql =
            "UPDATE replacement SET position = :position, context = :context, last_update = :lastUpdate WHERE id = :id";
        SqlParameterSource[] namedParameters = SqlParameterSourceUtils.createBatch(replacements.toArray());
        jdbcTemplate.batchUpdate(sql, namedParameters);
    }

    @Override
    public void removeReplacements(Collection<ReplacementModel> replacements) {
        String sql = "DELETE FROM replacement WHERE id IN (:ids)";
        Set<Long> ids = replacements
            .stream()
            .map(r -> Objects.requireNonNull(r.getId()))
            .collect(Collectors.toUnmodifiableSet());
        if (!ids.isEmpty()) {
            SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("ids", ids);
            jdbcTemplate.update(sql, namedParameters);
        }
    }

    @Override
    public long countReplacementsReviewed(WikipediaLanguage lang) {
        String sql =
            "SELECT COUNT(*) FROM replacement WHERE lang = :lang AND reviewer IS NOT NULL AND reviewer <> :system";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("lang", lang.getCode())
            .addValue("system", REVIEWER_SYSTEM);
        Long result = jdbcTemplate.queryForObject(sql, namedParameters, Long.class);
        return result == null ? 0L : result;
    }

    @Override
    public long countReplacementsNotReviewed(WikipediaLanguage lang) {
        String sql = "SELECT COUNT(*) FROM replacement WHERE lang = :lang AND reviewer IS NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("lang", lang.getCode());
        Long result = jdbcTemplate.queryForObject(sql, namedParameters, Long.class);
        return result == null ? 0L : result;
    }

    @Override
    public Map<String, Long> countReplacementsByReviewer(WikipediaLanguage lang) {
        String sql =
            "SELECT reviewer, COUNT(*) AS num FROM replacement " +
            "WHERE lang = :lang AND reviewer IS NOT NULL AND reviewer <> :system " +
            "GROUP BY reviewer";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("lang", lang.getCode())
            .addValue("system", REVIEWER_SYSTEM);
        return Objects.requireNonNull(
            jdbcTemplate.query(
                sql,
                namedParameters,
                rs -> {
                    Map<String, Long> map = new HashMap<>();
                    while (rs.next()) {
                        map.put(rs.getString("REVIEWER"), rs.getLong("NUM"));
                    }
                    return map;
                }
            )
        );
    }

    @Override
    public Collection<TypeSubtypeCount> countReplacementsByType(WikipediaLanguage lang) {
        String sql =
            "SELECT type, subtype, COUNT(DISTINCT article_id) AS num FROM replacement " +
            "WHERE lang = :lang AND reviewer IS NULL " +
            "GROUP BY lang, type, subtype";
        SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("lang", lang.getCode());
        return jdbcTemplate.query(
            sql,
            namedParameters,
            (resultSet, i) ->
                TypeSubtypeCount.of(
                    resultSet.getString("TYPE"),
                    resultSet.getString("SUBTYPE"),
                    resultSet.getLong("NUM")
                )
        );
    }

    @Override
    public void updateReviewerByType(WikipediaLanguage lang, String type, String subtype, String reviewer) {
        String sql =
            "UPDATE replacement SET reviewer=:system, last_update=:now " +
            "WHERE lang = :lang AND type = :type AND subtype = :subtype AND reviewer IS NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("system", reviewer)
            .addValue("now", LocalDate.now())
            .addValue("lang", lang.getCode())
            .addValue("type", type)
            .addValue("subtype", subtype);
        jdbcTemplate.update(sql, namedParameters);
    }

    @Override
    public void updateReviewerByPageAndType(
        WikipediaLanguage lang,
        int pageId,
        @Nullable String type,
        @Nullable String subtype,
        String reviewer
    ) {
        String sql =
            "UPDATE replacement SET reviewer=:reviewer, last_update=:now " +
            "WHERE lang = :lang AND article_id = :pageId AND reviewer IS NULL ";
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("reviewer", reviewer)
            .addValue("now", LocalDate.now())
            .addValue("lang", lang.getCode())
            .addValue("pageId", pageId);
        if (StringUtils.isNotBlank(type) && StringUtils.isNotBlank(subtype)) {
            sql += "AND type = :type AND subtype = :subtype";
            namedParameters = namedParameters.addValue("type", type).addValue("subtype", subtype);
        }
        jdbcTemplate.update(sql, namedParameters);
    }

    @Override
    public void removeReplacementsByType(WikipediaLanguage lang, String type, Collection<String> subtypes) {
        if (subtypes.isEmpty()) {
            throw new IllegalArgumentException();
        } else {
            String sql =
                "DELETE FROM replacement " +
                "WHERE lang = :lang AND type = :type AND subtype IN (:subtypes) AND reviewer IS NULL";
            SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("lang", lang.getCode())
                .addValue("type", type)
                .addValue("subtypes", subtypes);
            jdbcTemplate.update(sql, namedParameters);
        }
    }

    @Override
    public long findReplacementToReview(WikipediaLanguage lang, long chunkSize) {
        String sql =
            "SELECT FLOOR(MIN(id) + (MAX(id) - MIN(id) + 1 - :chunkSize) * RAND()) FROM replacement " +
            "WHERE lang = :lang AND reviewer IS NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("chunkSize", chunkSize)
            .addValue("lang", lang.getCode());
        Long result = jdbcTemplate.queryForObject(sql, namedParameters, Long.class);
        return result == null ? 0L : result;
    }
}
