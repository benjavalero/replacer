package es.bvalero.replacer.replacement;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Loggable(Loggable.TRACE) // To warn about performance issues
@Repository
@Transactional
class ReplacementDaoImpl implements ReplacementDao, ReplacementStatsDao {

    private static final String PARAM_PAGE_ID = "pageId";
    private static final String PARAM_LANG = "lang";
    private static final String PARAM_TYPE = "type";
    private static final String PARAM_SUBTYPE = "subtype";
    private static final String PARAM_REVIEWER = "reviewer";
    private static final String PARAM_VALUE_SYSTEM = "system";

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    ///// PAGE REVIEW

    @Override
    public void reviewByPageId(
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
            .addValue(PARAM_REVIEWER, reviewer)
            .addValue("now", LocalDate.now())
            .addValue(PARAM_LANG, lang.getCode())
            .addValue(PARAM_PAGE_ID, pageId);
        if (StringUtils.isNotBlank(type) && StringUtils.isNotBlank(subtype)) {
            sql += "AND type = :type AND subtype = :subtype";
            namedParameters = namedParameters.addValue(PARAM_TYPE, type).addValue(PARAM_SUBTYPE, subtype);
        }
        jdbcTemplate.update(sql, namedParameters);
    }

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

    @Override
    @Loggable(value = Loggable.TRACE, limit = 10, unit = TimeUnit.SECONDS)
    public LanguageCount countReplacementsGroupedByType(WikipediaLanguage lang) {
        return LanguageCount.build(countPagesGroupedByTypeAndSubtype(lang));
    }

    private List<TypeSubtypeCount> countPagesGroupedByTypeAndSubtype(WikipediaLanguage lang) {
        String sql =
            "SELECT type, subtype, COUNT(DISTINCT article_id) AS num FROM replacement " +
            "WHERE lang = :lang AND reviewer IS NULL " +
            "GROUP BY lang, type, subtype";
        SqlParameterSource namedParameters = new MapSqlParameterSource().addValue(PARAM_LANG, lang.getCode());
        return jdbcTemplate.query(sql, namedParameters, new TypeSubtypeCountRowMapper());
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
