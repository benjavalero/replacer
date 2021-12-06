package es.bvalero.replacer.replacement.stats;

import static es.bvalero.replacer.repository.ReplacementRepository.REVIEWER_SYSTEM;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Loggable(Loggable.TRACE) // To warn about performance issues
@Repository
@Transactional
class ReplacementStatsJdbcRepository implements ReplacementStatsRepository {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

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
    public Collection<ReviewerCount> countReplacementsGroupedByReviewer(WikipediaLanguage lang) {
        String sql =
            "SELECT reviewer, COUNT(*) AS num FROM replacement " +
            "WHERE lang = :lang AND reviewer IS NOT NULL AND reviewer <> :system " +
            "GROUP BY reviewer " +
            "ORDER BY COUNT(*) DESC";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("lang", lang.getCode())
            .addValue("system", REVIEWER_SYSTEM);
        return jdbcTemplate.query(sql, namedParameters, new ReviewerCountRowMapper());
    }
}
