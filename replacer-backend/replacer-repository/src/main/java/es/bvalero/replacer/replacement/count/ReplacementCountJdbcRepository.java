package es.bvalero.replacer.replacement.count;

import static es.bvalero.replacer.replacement.IndexedReplacement.REVIEWER_SYSTEM;

import es.bvalero.replacer.common.domain.ResultCount;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.page.IndexedPage;
import es.bvalero.replacer.page.PageKey;
import java.time.LocalDate;
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
        String sqlReplacement =
            "SELECT COUNT(*) AS num FROM replacement WHERE lang = :lang AND reviewer IS NOT NULL AND reviewer <> :system";
        String sqlCustom = "SELECT COUNT(*) AS num FROM custom WHERE lang = :lang";
        String sqlUnion = String.format("(%s UNION %s) AS sqlUnion", sqlReplacement, sqlCustom);
        String sql = String.format("SELECT SUM(num) FROM %s GROUP BY NULL", sqlUnion);

        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("lang", lang.getCode())
            .addValue("system", REVIEWER_SYSTEM);
        Integer result = jdbcTemplate.queryForObject(sql, namedParameters, Integer.class);
        return Objects.requireNonNullElse(result, 0);
    }

    @Override
    public int countNotReviewed(WikipediaLanguage lang) {
        String sql = "SELECT COUNT(*) FROM replacement WHERE lang = :lang AND reviewer IS NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("lang", lang.getCode());
        Integer result = jdbcTemplate.queryForObject(sql, namedParameters, Integer.class);
        return Objects.requireNonNullElse(result, 0);
    }

    @Override
    public Collection<ResultCount<String>> countReviewedGroupedByReviewer(WikipediaLanguage lang) {
        String sqlReplacement =
            "SELECT reviewer, COUNT(*) AS num FROM replacement " +
            "WHERE lang = :lang AND reviewer IS NOT NULL AND reviewer <> :system " +
            "GROUP BY reviewer";
        String sqlCustom = "SELECT reviewer, COUNT(*) AS num FROM custom WHERE lang = :lang GROUP BY reviewer";
        String sqlUnion = String.format("(%s UNION %s) AS sqlUnion", sqlReplacement, sqlCustom);
        String sql =
            String.format("SELECT reviewer, SUM(num) AS numSum FROM %s ", sqlUnion) +
            "GROUP BY reviewer ORDER BY SUM(num) DESC";

        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("lang", lang.getCode())
            .addValue("system", REVIEWER_SYSTEM);
        return Objects.requireNonNull(
            jdbcTemplate.query(
                sql,
                namedParameters,
                (resultSet, rowNum) -> ResultCount.of(resultSet.getString("REVIEWER"), resultSet.getInt("NUMSUM"))
            )
        );
    }

    @Override
    public Collection<ResultCount<IndexedPage>> countNotReviewedGroupedByPage(WikipediaLanguage lang, int numResults) {
        String sql =
            "SELECT p.page_id, p.title, COUNT(*) AS num " +
            "FROM replacement r JOIN page p ON p.lang = r.lang AND p.page_id = r.page_id " +
            "WHERE r.lang = :lang AND r.reviewer IS NULL " +
            "GROUP BY p.page_id, p.title " +
            "ORDER BY num DESC " +
            "LIMIT " +
            numResults;
        SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("lang", lang.getCode());
        return Objects.requireNonNull(
            jdbcTemplate.query(
                sql,
                namedParameters,
                (resultSet, rowNum) ->
                    ResultCount.of(
                        IndexedPage.builder()
                            .pageKey(PageKey.of(lang, resultSet.getInt("PAGE_ID")))
                            .title(resultSet.getString("TITLE"))
                            .lastUpdate(LocalDate.now()) // Not relevant in this method
                            .build(),
                        resultSet.getInt("NUM")
                    )
            )
        );
    }
}
