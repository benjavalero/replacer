package es.bvalero.replacer.replacement.count;

import static es.bvalero.replacer.page.IndexedReplacement.REVIEWER_SYSTEM;

import es.bvalero.replacer.common.domain.PageTitle;
import es.bvalero.replacer.common.domain.ResultCount;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Collection;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Qualifier("replacementCountJdbcRepository")
@Transactional
@Repository
class ReplacementCountJdbcRepository implements ReplacementCountRepository {

    // Dependency injection
    private final NamedParameterJdbcTemplate jdbcTemplate;

    ReplacementCountJdbcRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int countReviewed(WikipediaLanguage lang) {
        LOGGER.debug("START Counting reviewed pages - {}…", lang);
        String sqlReviewed = "SELECT COUNT(*) AS num FROM reviewed WHERE lang = :lang AND reviewer <> :system";
        String sqlCustom = "SELECT COUNT(*) AS num FROM custom WHERE lang = :lang";
        String sqlUnion = String.format("(%s UNION %s) AS sqlUnion", sqlReviewed, sqlCustom);
        String sql = String.format("SELECT SUM(num) FROM %s GROUP BY NULL", sqlUnion);

        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("lang", lang.getCode())
            .addValue("system", REVIEWER_SYSTEM);
        Integer result = jdbcTemplate.queryForObject(sql, namedParameters, Integer.class);
        LOGGER.debug("END Counting reviewed pages - {}", lang);
        return Objects.requireNonNullElse(result, 0);
    }

    @Override
    public int countNotReviewed(WikipediaLanguage lang) {
        LOGGER.debug("START Counting not reviewed pages - {}…", lang);
        String sql = "SELECT COUNT(*) FROM replacement WHERE lang = :lang";
        SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("lang", lang.getCode());
        Integer result = jdbcTemplate.queryForObject(sql, namedParameters, Integer.class);
        LOGGER.debug("END Counting not reviewed pages - {}", lang);
        return Objects.requireNonNullElse(result, 0);
    }

    @Override
    public Collection<ResultCount<String>> countReviewedGroupedByReviewer(WikipediaLanguage lang) {
        LOGGER.debug("START Counting reviewed pages grouped by reviewer - {}…", lang);
        String sqlReviewed = """
            SELECT reviewer, COUNT(*) AS num FROM reviewed
            WHERE lang = :lang AND reviewer <> :system
            GROUP BY reviewer
            """;
        String sqlCustom = "SELECT reviewer, COUNT(*) AS num FROM custom WHERE lang = :lang GROUP BY reviewer";
        String sqlUnion = String.format("(%s UNION %s) AS sqlUnion", sqlReviewed, sqlCustom);
        String sql =
            String.format("SELECT reviewer, SUM(num) AS numSum FROM %s ", sqlUnion) +
            "GROUP BY reviewer ORDER BY SUM(num) DESC";

        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("lang", lang.getCode())
            .addValue("system", REVIEWER_SYSTEM);
        Collection<ResultCount<String>> counts = Objects.requireNonNull(
            jdbcTemplate.query(sql, namedParameters, (resultSet, rowNum) ->
                ResultCount.of(resultSet.getString("REVIEWER"), resultSet.getInt("NUMSUM"))
            )
        );
        LOGGER.debug("END Counting reviewed pages grouped by reviewer - {}", lang);
        return counts;
    }

    @Override
    public Collection<ResultCount<PageTitle>> countNotReviewedGroupedByPage(WikipediaLanguage lang, int numResults) {
        LOGGER.debug("START Counting not reviewed pages grouped by page - {}…", lang);
        String sql =
            "SELECT sub.page_id, p.title, sub.num " +
            "FROM (SELECT page_id, COUNT(*) AS num " +
            "FROM replacement WHERE lang = :lang " +
            "GROUP BY page_id ORDER BY num DESC LIMIT " +
            numResults +
            ") sub " +
            "JOIN page p ON p.lang = :lang AND p.page_id = sub.page_id";
        SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("lang", lang.getCode());
        Collection<ResultCount<PageTitle>> counts = Objects.requireNonNull(
            jdbcTemplate.query(sql, namedParameters, (resultSet, rowNum) ->
                ResultCount.of(
                    PageTitle.of(resultSet.getInt("PAGE_ID"), resultSet.getString("TITLE")),
                    resultSet.getInt("NUM")
                )
            )
        );
        LOGGER.debug("END Counting not reviewed pages grouped by page - {}", lang);
        return counts;
    }
}
