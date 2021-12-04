package es.bvalero.replacer.replacement;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Loggable(Loggable.TRACE) // To warn about performance issues
@Repository
@Transactional
class ReplacementDaoImpl implements ReplacementDao, ReplacementStatsDao {

    private static final String PARAM_LANG = "lang";
    private static final String PARAM_TYPE = "type";
    private static final String PARAM_VALUE_SYSTEM = "system";

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    ///// STATISTICS

    @Override
    public long countReplacementsReviewed(WikipediaLanguage lang) {
        String sql =
            "SELECT COUNT(*) FROM replacement WHERE lang = :lang AND reviewer IS NOT NULL AND reviewer <> :system";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue(PARAM_LANG, lang.getCode())
            .addValue(PARAM_VALUE_SYSTEM, ReplacementEntity.REVIEWER_SYSTEM);
        Long result = jdbcTemplate.queryForObject(sql, namedParameters, Long.class);
        return result == null ? 0L : result;
    }

    // This count is also used to guess the total for the review without type. Not worth to DISTINCT.
    @Override
    public long countReplacementsNotReviewed(WikipediaLanguage lang) {
        String sql = "SELECT COUNT(*) FROM replacement WHERE lang = :lang AND reviewer IS NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource().addValue(PARAM_LANG, lang.getCode());
        Long result = jdbcTemplate.queryForObject(sql, namedParameters, Long.class);
        return result == null ? 0L : result;
    }

    @Override
    public List<ReviewerCount> countReplacementsGroupedByReviewer(WikipediaLanguage lang) {
        String sql =
            "SELECT reviewer, COUNT(*) AS num FROM replacement " +
            "WHERE lang = :lang AND reviewer IS NOT NULL AND reviewer <> :system " +
            "GROUP BY reviewer " +
            "ORDER BY COUNT(*) DESC";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue(PARAM_LANG, lang.getCode())
            .addValue(PARAM_VALUE_SYSTEM, ReplacementEntity.REVIEWER_SYSTEM);
        return jdbcTemplate.query(sql, namedParameters, new ReviewerCountRowMapper());
    }

    ///// MISSPELLING MANAGER

    @Override
    public void deleteToBeReviewedBySubtype(WikipediaLanguage lang, String type, Set<String> subtypes) {
        String sql =
            "DELETE FROM replacement " +
            "WHERE lang = :lang AND type = :type AND subtype IN (:subtypes) AND reviewer IS NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue(PARAM_LANG, lang.getCode())
            .addValue(PARAM_TYPE, type)
            .addValue("subtypes", subtypes);
        jdbcTemplate.update(sql, namedParameters);
    }
}
