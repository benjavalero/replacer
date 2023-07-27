package es.bvalero.replacer.page;

import es.bvalero.replacer.common.domain.ResultCount;
import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@SuppressWarnings("java:S1192")
@Qualifier("pageJdbcRepository")
@Transactional
@Repository
class PageJdbcRepository implements PageRepository, PageCountRepository {

    private static final String FROM_REPLACEMENT_JOIN_PAGE =
        "FROM page p LEFT JOIN replacement r ON p.lang = r.lang AND p.page_id = r.page_id ";

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public Optional<IndexedPage> findPageByKey(PageKey pageKey) {
        String sql =
            "SELECT p.lang, p.page_id, p.title, p.last_update, " +
            "r.id, r.kind, r.subtype, r.start, r.context, r.reviewer " +
            FROM_REPLACEMENT_JOIN_PAGE +
            "WHERE p.lang = :lang AND p.page_id = :pageId";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("lang", pageKey.getLang().getCode())
            .addValue("pageId", pageKey.getPageId());
        Collection<IndexedPage> pages = jdbcTemplate.query(sql, namedParameters, new PageResultExtractor());
        return Objects.requireNonNull(pages).stream().findAny();
    }

    @Override
    public Collection<IndexedPage> findPagesByIdInterval(WikipediaLanguage lang, int minPageId, int maxPageId) {
        String sql =
            "SELECT p.lang, p.page_id, p.title, p.last_update, " +
            "r.id, r.kind, r.subtype, r.start, r.context, r.reviewer " +
            FROM_REPLACEMENT_JOIN_PAGE +
            "WHERE p.lang = :lang AND p.page_id BETWEEN :minPageId AND :maxPageId";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("lang", lang.getCode())
            .addValue("minPageId", minPageId)
            .addValue("maxPageId", maxPageId);
        return Objects
            .requireNonNull(jdbcTemplate.query(sql, namedParameters, new PageResultExtractor()))
            .stream()
            .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public void add(Collection<IndexedPage> pages) {
        String sql =
            "INSERT INTO page (lang, page_id, title, last_update) " +
            "VALUES (:pageKey.lang.code, :pageKey.pageId, :title, :lastUpdate)";
        SqlParameterSource[] namedParameters = SqlParameterSourceUtils.createBatch(pages.toArray());
        jdbcTemplate.batchUpdate(sql, namedParameters);
    }

    @Override
    public void update(Collection<IndexedPage> pages) {
        String sql =
            "UPDATE page SET title = :title, last_update = :lastUpdate " +
            "WHERE lang = :pageKey.lang.code AND page_id = :pageKey.pageId";
        SqlParameterSource[] namedParameters = SqlParameterSourceUtils.createBatch(pages.toArray());
        jdbcTemplate.batchUpdate(sql, namedParameters);
    }

    @Override
    public void updateLastUpdate(PageKey pageKey, LocalDate lastUpdate) {
        String sql = "UPDATE page SET last_update = :lastUpdate WHERE lang = :lang AND page_id = :pageId";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("lastUpdate", lastUpdate)
            .addValue("lang", pageKey.getLang().getCode())
            .addValue("pageId", pageKey.getPageId());
        jdbcTemplate.update(sql, namedParameters);
    }

    @Override
    public void removeByKey(Collection<PageKey> pageKeys) {
        // No need to delete first the replacements as they are deleted on cascade by the database
        SqlParameterSource[] namedParameters = SqlParameterSourceUtils.createBatch(pageKeys.toArray());
        String sqlPages = "DELETE FROM page WHERE lang = :lang.code AND page_id = :pageId";
        jdbcTemplate.batchUpdate(sqlPages, namedParameters);
    }

    @Override
    public Collection<PageKey> findNotReviewed(WikipediaLanguage lang, @Nullable StandardType type, int numResults) {
        // For the sake of optimization, we use different queries to find by type or by no-type.
        if (type == null) {
            return findNotReviewedByNoType(lang, numResults);
        } else {
            return findNotReviewedByType(lang, type, numResults);
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

    private Collection<PageKey> findNotReviewedByType(WikipediaLanguage lang, StandardType type, int numResults) {
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
    public int countNotReviewedByType(WikipediaLanguage lang, @Nullable StandardType type) {
        // This approach is slightly better than using a JOIN with the page table
        String sql = "SELECT COUNT (DISTINCT page_id) FROM replacement WHERE lang = :lang AND reviewer IS NULL";
        MapSqlParameterSource namedParameters = new MapSqlParameterSource().addValue("lang", lang.getCode());
        if (type != null) {
            sql += " AND kind = :kind AND subtype = :subtype";
            namedParameters.addValue("kind", type.getKind().getCode()).addValue("subtype", type.getSubtype());
        }
        Integer result = jdbcTemplate.queryForObject(sql, namedParameters, Integer.class);
        return Objects.requireNonNullElse(result, 0);
    }

    @Override
    public Collection<String> findPageTitlesNotReviewedByType(WikipediaLanguage lang, StandardType type) {
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

    @Override
    public Collection<ResultCount<StandardType>> countPagesNotReviewedByType(WikipediaLanguage lang) {
        // Using the index this approach is better than executing several queries by kind
        String sql =
            "SELECT kind, subtype, COUNT(DISTINCT page_id) AS num FROM replacement " +
            "WHERE lang = :lang AND reviewer IS NULL " +
            "GROUP BY kind, subtype";
        SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("lang", lang.getCode());
        return jdbcTemplate.query(
            sql,
            namedParameters,
            (resultSet, rowNum) ->
                ResultCount.of(
                    StandardType.of(resultSet.getByte("KIND"), resultSet.getString("SUBTYPE")),
                    resultSet.getInt("NUM")
                )
        );
    }
}
