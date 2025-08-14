package es.bvalero.replacer.page;

import es.bvalero.replacer.common.domain.PageKey;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.StandardType;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@SuppressWarnings("java:S1192")
@Qualifier("pageJdbcRepository")
@Transactional
@Repository
class PageJdbcRepository implements PageRepository {

    private static final String FROM_REPLACEMENT_JOIN_PAGE =
        "FROM page p LEFT JOIN replacement r ON p.lang = r.lang AND p.page_id = r.page_id ";

    // Dependency injection
    private final NamedParameterJdbcTemplate jdbcTemplate;

    PageJdbcRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<IndexedPage> findByKey(PageKey pageKey) {
        String sql =
            "SELECT p.lang, p.page_id, p.title, p.last_update, " +
            "r.id, r.kind, r.subtype, r.start, r.context, r.reviewer, " +
            "r.review_type, r.review_timestamp, r.old_rev_id, r.new_rev_id " +
            FROM_REPLACEMENT_JOIN_PAGE +
            "WHERE p.lang = :lang AND p.page_id = :pageId";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("lang", pageKey.getLang().getCode())
            .addValue("pageId", pageKey.getPageId());
        Collection<IndexedPage> pages = jdbcTemplate.query(sql, namedParameters, new PageResultExtractor());
        return Objects.requireNonNull(pages).stream().findAny();
    }

    @Override
    public Collection<IndexedPage> findByIdRange(WikipediaLanguage lang, int minPageId, int maxPageId) {
        String sql =
            "SELECT p.lang, p.page_id, p.title, p.last_update, " +
            "r.id, r.kind, r.subtype, r.start, r.context, r.reviewer, " +
            "r.review_type, r.review_timestamp, r.old_rev_id, r.new_rev_id " +
            FROM_REPLACEMENT_JOIN_PAGE +
            "WHERE p.lang = :lang AND p.page_id BETWEEN :minPageId AND :maxPageId";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("lang", lang.getCode())
            .addValue("minPageId", minPageId)
            .addValue("maxPageId", maxPageId);
        return Objects.requireNonNull(jdbcTemplate.query(sql, namedParameters, new PageResultExtractor()))
            .stream()
            .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Collection<PageKey> findNotReviewedByType(
        WikipediaLanguage lang,
        @Nullable StandardType type,
        int numResults
    ) {
        // For the sake of optimization, we use different queries to find by type or by no-type.
        if (type == null) {
            return findNotReviewedByNoType(lang, numResults);
        } else {
            return findNotReviewedByStandardType(lang, type, numResults);
        }
    }

    private Collection<PageKey> findNotReviewedByNoType(WikipediaLanguage lang, int numResults) {
        // Find a random page without filtering by type takes a lot
        // Instead we find a random replacement and then the following pages
        int randomStart = findRandomReplacementIdNotReviewed(lang, numResults);

        // Not worth to DISTINCT. Instead, we return the results as a set to avoid duplicates.
        String sql =
            "SELECT page_id FROM replacement " +
            "WHERE lang = :lang AND reviewer IS NULL AND id > :start " +
            "ORDER BY id " +
            "LIMIT " +
            numResults;
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("lang", lang.getCode())
            .addValue("start", randomStart);
        return jdbcTemplate
            .queryForList(sql, namedParameters, Integer.class)
            .stream()
            .map(pageId -> PageKey.of(lang, pageId))
            .collect(Collectors.toUnmodifiableSet());
    }

    private int findRandomReplacementIdNotReviewed(WikipediaLanguage lang, int chunkSize) {
        String sql =
            "SELECT FLOOR(MIN(id) + (MAX(id) - MIN(id) + 1 - :chunkSize) * RAND()) FROM replacement " +
            "WHERE lang = :lang AND reviewer IS NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("chunkSize", chunkSize)
            .addValue("lang", lang.getCode());
        Integer result = jdbcTemplate.queryForObject(sql, namedParameters, Integer.class);
        return Objects.requireNonNullElse(result, 0);
    }

    private Collection<PageKey> findNotReviewedByStandardType(
        WikipediaLanguage lang,
        StandardType type,
        int numResults
    ) {
        // When filtering by type/subtype ORDER BY RAND() still takes a while, but it is admissible.
        // Not worth to DISTINCT. Instead, we return the results as a set to avoid duplicates.
        String sql =
            "SELECT page_id FROM replacement " +
            "WHERE lang = :lang AND kind = :kind AND subtype = :subtype AND reviewer IS NULL " +
            "ORDER BY RAND() " +
            "LIMIT " +
            numResults;
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("lang", lang.getCode())
            .addValue("kind", type.getKind().getCode())
            .addValue("subtype", type.getSubtype());
        return jdbcTemplate
            .queryForList(sql, namedParameters, Integer.class)
            .stream()
            .map(pageId -> PageKey.of(lang, pageId))
            .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Collection<String> findTitlesNotReviewedByType(WikipediaLanguage lang, StandardType type) {
        String sql =
            "SELECT p.title FROM page p " +
            "WHERE p.lang = :lang AND EXISTS " +
            "(SELECT NULL FROM replacement r WHERE p.lang = r.lang AND p.page_id = r.page_id " +
            "AND r.kind = :kind AND r.subtype = :subtype AND r.reviewer IS NULL)";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("lang", lang.getCode())
            .addValue("kind", type.getKind().getCode())
            .addValue("subtype", type.getSubtype());
        return jdbcTemplate.queryForList(sql, namedParameters, String.class);
    }
}
