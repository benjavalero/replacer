package es.bvalero.replacer.replacement;

import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class ReplacementDao {
    private static final String PARAM_LANG = "lang";
    private static final String PARAM_SYSTEM = "system";

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public List<ReplacementEntity> findByPages(int minId, int maxId, WikipediaLanguage lang) {
        // We need all the fields but the title so we don't select it to improve performance
        String sql =
            "SELECT id, article_id, lang, type, subtype, position, context, last_update, reviewer, NULL AS title " +
            "FROM replacement2 WHERE lang = :lang AND article_id BETWEEN :minId AND :maxId";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue(PARAM_LANG, lang.getCode())
            .addValue("minId", minId)
            .addValue("maxId", maxId);
        return jdbcTemplate.query(sql, namedParameters, new ReplacementRowMapper());
    }

    public void deleteObsoleteReplacements(WikipediaLanguage lang, Set<Integer> pageIds) {
        String sql =
            "DELETE FROM replacement2 " +
            "WHERE lang = :lang AND article_id IN (:pageIds) AND (reviewer IS NULL OR reviewer = :system)";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue(PARAM_SYSTEM, ReplacementIndexService.SYSTEM_REVIEWER)
            .addValue(PARAM_LANG, lang.getCode())
            .addValue("pageIds", pageIds);
        jdbcTemplate.update(sql, namedParameters);
    }

    public void reviewPagesReplacementsAsSystem(WikipediaLanguage lang, String type, String subtype) {
        String sql =
            "UPDATE replacement2 SET reviewer=:system, last_update=:now " +
            "WHERE lang = :lang AND type = :type AND subtype = :subtype AND reviewer IS NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue(PARAM_SYSTEM, ReplacementIndexService.SYSTEM_REVIEWER)
            .addValue("now", LocalDate.now())
            .addValue(PARAM_LANG, lang.getCode())
            .addValue("type", type)
            .addValue("subtype", subtype);
        jdbcTemplate.update(sql, namedParameters);
    }

    public Long countByLangAndReviewed(WikipediaLanguage lang) {
        String sql =
            "SELECT COUNT(*) FROM replacement2 " +
            "WHERE lang = :lang AND reviewer IS NOT NULL AND reviewer <> :system";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue(PARAM_LANG, lang.getCode())
            .addValue(PARAM_SYSTEM, ReplacementIndexService.SYSTEM_REVIEWER);
        return jdbcTemplate.queryForObject(sql, namedParameters, Long.class);
    }

    // Not worth to DISTINCT. Besides this count is also used in statistics.
    public Long countByLangAndReviewerIsNull(WikipediaLanguage lang) {
        String sql = "SELECT COUNT(*) FROM replacement2 " + "WHERE lang = :lang AND reviewer IS NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource().addValue(PARAM_LANG, lang.getCode());
        return jdbcTemplate.queryForObject(sql, namedParameters, Long.class);
    }

    public List<ReviewerCount> countGroupedByReviewer(WikipediaLanguage lang) {
        String sql =
            "SELECT reviewer, COUNT(*) AS num FROM replacement2 " +
            "WHERE lang = :lang AND reviewer IS NOT NULL AND reviewer <> :system " +
            "GROUP BY reviewer " +
            "ORDER BY COUNT(*) DESC";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue(PARAM_LANG, lang.getCode())
            .addValue(PARAM_SYSTEM, ReplacementIndexService.SYSTEM_REVIEWER);
        return jdbcTemplate.query(sql, namedParameters, new ReviewerCountRowMapper());
    }

    public List<TypeSubtypeCount> countGroupedByTypeAndSubtype() {
        String sql =
            "SELECT lang, type, subtype, COUNT(DISTINCT article_id) AS num FROM replacement2 " +
            "WHERE reviewer IS NULL " +
            "GROUP BY lang, type, subtype";
        return jdbcTemplate.query(sql, new TypeSubtypeCountRowMapper());
    }
}
