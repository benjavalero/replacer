package es.bvalero.replacer.replacement;

import static es.bvalero.replacer.replacement.IndexedReplacement.REVIEWER_SYSTEM;

import es.bvalero.replacer.common.domain.ResultCount;
import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.page.IndexedPage;
import es.bvalero.replacer.page.PageKey;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Qualifier("replacementJdbcRepository")
@Transactional
@Repository
class ReplacementJdbcRepository implements ReplacementSaveRepository, ReplacementCountRepository {

    // Dependency injection
    private final NamedParameterJdbcTemplate jdbcTemplate;

    ReplacementJdbcRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void add(Collection<IndexedReplacement> replacements) {
        String sql =
            "INSERT INTO replacement (page_id, lang, kind, subtype, start, context, reviewer) " +
            "VALUES (:pageKey.pageId, :pageKey.lang.code, :type.kind.code, :type.subtype, :start, :context, :reviewer)";
        SqlParameterSource[] namedParameters = SqlParameterSourceUtils.createBatch(replacements.toArray());
        jdbcTemplate.batchUpdate(sql, namedParameters);
    }

    @Override
    public void update(Collection<IndexedReplacement> replacements) {
        String sql = "UPDATE replacement SET start = :start, context = :context WHERE id = :id";
        SqlParameterSource[] namedParameters = SqlParameterSourceUtils.createBatch(replacements.toArray());
        jdbcTemplate.batchUpdate(sql, namedParameters);
    }

    @Override
    public void remove(Collection<IndexedReplacement> replacements) {
        if (!replacements.isEmpty()) {
            String sql = "DELETE FROM replacement WHERE id IN (:ids)";
            Set<Integer> ids = replacements
                .stream()
                .map(r -> Objects.requireNonNull(r.getId()))
                .collect(Collectors.toUnmodifiableSet());
            SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("ids", ids);
            jdbcTemplate.update(sql, namedParameters);
        }
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
                        IndexedPage
                            .builder()
                            .pageKey(PageKey.of(lang, resultSet.getInt("PAGE_ID")))
                            .title(resultSet.getString("TITLE"))
                            .lastUpdate(LocalDate.now()) // Not relevant in this method
                            .build(),
                        resultSet.getInt("NUM")
                    )
            )
        );
    }

    @Override
    public void updateReviewerByType(WikipediaLanguage lang, StandardType type, String reviewer) {
        String sql =
            "UPDATE replacement SET reviewer=:reviewer " +
            "WHERE lang = :lang AND kind = :kind AND subtype = :subtype AND reviewer IS NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("reviewer", reviewer)
            .addValue("lang", lang.getCode())
            .addValue("kind", type.getKind().getCode())
            .addValue("subtype", type.getSubtype());
        jdbcTemplate.update(sql, namedParameters);
    }

    @Override
    public void updateReviewer(Collection<IndexedReplacement> replacements) {
        replacements.forEach(this::updateReviewer);
    }

    private void updateReviewer(IndexedReplacement replacement) {
        String sql =
            "UPDATE replacement SET reviewer = :reviewer " +
            "WHERE lang = :lang AND page_id = :pageId AND kind = :kind AND subtype = :subtype " +
            "AND start = :start AND reviewer IS NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("reviewer", replacement.getReviewer())
            .addValue("lang", replacement.getPageKey().getLang().getCode())
            .addValue("pageId", replacement.getPageKey().getPageId())
            .addValue("kind", replacement.getType().getKind().getCode())
            .addValue("subtype", replacement.getType().getSubtype())
            .addValue("start", replacement.getStart());
        int numRows = jdbcTemplate.update(sql, namedParameters);
        if (numRows != 1) {
            LOGGER.warn("Indexed Replacement reviewer not updated: {}", replacement);
        }
    }

    @Override
    public void removeByType(WikipediaLanguage lang, StandardType type) {
        String sql =
            "DELETE FROM replacement " +
            "WHERE lang = :lang AND kind = :kind AND subtype = :subtype AND reviewer IS NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("lang", lang.getCode())
            .addValue("kind", type.getKind().getCode())
            .addValue("subtype", type.getSubtype());
        jdbcTemplate.update(sql, namedParameters);
    }
}
