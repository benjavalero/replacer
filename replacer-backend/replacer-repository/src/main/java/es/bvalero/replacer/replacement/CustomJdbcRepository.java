package es.bvalero.replacer.replacement;

import es.bvalero.replacer.common.domain.PageKey;
import es.bvalero.replacer.common.domain.ResultCount;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.CustomType;
import es.bvalero.replacer.finder.StandardType;
import es.bvalero.replacer.page.IndexedCustomReplacement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Repository
class CustomJdbcRepository implements CustomRepository {

    // Dependency injection
    private final NamedParameterJdbcTemplate jdbcTemplate;

    CustomJdbcRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Using DISTINCT makes the query not to use to wanted index "idx_count"
    @Override
    public Collection<PageKey> findPagesReviewed(WikipediaLanguage lang, CustomType type) {
        String sql = "SELECT page_id FROM custom WHERE lang = :lang AND replacement = :replacement AND cs = :cs";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("lang", lang.getCode())
            .addValue("replacement", type.getSubtype())
            .addValue("cs", IndexedCustomReplacement.getCs(type.isCaseSensitive()));
        return jdbcTemplate
            .queryForList(sql, namedParameters, Integer.class)
            .stream()
            .map(pageId -> PageKey.of(lang, pageId))
            .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Collection<CustomType> findDistinctCustomTypes(WikipediaLanguage lang) {
        String sql = "SELECT DISTINCT replacement, cs FROM custom WHERE lang = :lang";
        SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("lang", lang.getCode());
        return jdbcTemplate.query(sql, namedParameters, (rs, rowNum) ->
            CustomType.of(rs.getString("replacement"), rs.getByte("cs") == 1)
        );
    }

    @Override
    public Collection<ResultCount<CustomType>> countReviewedGroupedByType(WikipediaLanguage lang, int numResults) {
        String sql =
            "SELECT replacement, cs, COUNT(*) AS num FROM custom " +
            "WHERE lang = :lang " +
            "GROUP BY replacement, cs " +
            "ORDER BY num DESC " +
            "LIMIT " +
            numResults;
        SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("lang", lang.getCode());
        return Objects.requireNonNull(
            jdbcTemplate.query(sql, namedParameters, (rs, rowNum) ->
                ResultCount.of(CustomType.of(rs.getString("REPLACEMENT"), rs.getByte("CS") == 1), rs.getInt("NUM"))
            )
        );
    }

    /**
     * The context is set to empty string because the custom table doesn't store context.
     * This is safe: the PageComparator treats blank contexts as non-comparable (hasSameContext returns false),
     * so these migrated replacements will only match by position (hasSameStart).
     * Since they are already reviewed (reviewer is NOT NULL), they won't be marked as obsolete
     * during reindexation, and the next indexation will update them with a proper context if found.
     *
     * Uses batch operations leveraging MariaDB's rewriteBatchedStatements=true to minimize round-trips.
     */
    @Override
    public int migrateToReplacement(WikipediaLanguage lang, Map<CustomType, StandardType> migrations) {
        if (migrations.isEmpty()) {
            return 0;
        }

        String insertSql =
            "INSERT INTO replacement (lang, page_id, kind, subtype, start, context, reviewer, review_type, review_timestamp, old_rev_id, new_rev_id) " +
            "SELECT lang, page_id, :kind, :subtype, start, '', reviewer, review_type, review_timestamp, old_rev_id, new_rev_id " +
            "FROM custom WHERE lang = :lang AND replacement = :replacement AND cs = :cs";

        String deleteSql = "DELETE FROM custom WHERE lang = :lang AND replacement = :replacement AND cs = :cs";

        SqlParameterSource[] params = migrations
            .entrySet()
            .stream()
            .map(entry ->
                new MapSqlParameterSource()
                    .addValue("lang", lang.getCode())
                    .addValue("replacement", entry.getKey().getSubtype())
                    .addValue("cs", IndexedCustomReplacement.getCs(entry.getKey().isCaseSensitive()))
                    .addValue("kind", entry.getValue().getKind().getCode())
                    .addValue("subtype", entry.getValue().getSubtype())
            )
            .toArray(SqlParameterSource[]::new);

        int[] insertCounts = jdbcTemplate.batchUpdate(insertSql, params);
        jdbcTemplate.batchUpdate(deleteSql, params);

        return Arrays.stream(insertCounts).sum();
    }
}
