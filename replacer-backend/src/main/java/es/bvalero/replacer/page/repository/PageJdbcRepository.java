package es.bvalero.replacer.page.repository;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaPageId;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Primary
@Loggable(Loggable.TRACE) // To warn about performance issues
@Transactional
@Repository
@Qualifier("pageJdbcRepository")
class PageJdbcRepository implements PageRepository, PageReviewRepository {

    private static final String FROM_REPLACEMENT_JOIN_PAGE =
        "FROM replacement r JOIN page p ON r.lang = p.lang AND r.article_id = p.article_id ";

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public Optional<PageModel> findByPageId(WikipediaPageId id) {
        String sql =
            "SELECT r.id, r.lang, r.article_id, r.type, r.subtype, r.position, r.context, r.last_update, r.reviewer, p.title " +
            FROM_REPLACEMENT_JOIN_PAGE +
            "WHERE r.lang = :lang AND r.article_id = :pageId";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("lang", id.getLang().getCode())
            .addValue("pageId", id.getPageId());
        Collection<PageModel> pages = jdbcTemplate.query(sql, namedParameters, new PageResultExtractor());
        return Objects.requireNonNull(pages).stream().findAny();
    }

    @Override
    public Collection<PageModel> findByPageIdInterval(WikipediaLanguage lang, int minPageId, int maxPageId) {
        String sql =
            "SELECT r.id, r.lang, r.article_id, r.type, r.subtype, r.position, r.context, r.last_update, r.reviewer, p.title " +
            FROM_REPLACEMENT_JOIN_PAGE +
            "WHERE r.lang = :lang AND r.article_id BETWEEN :minPageId AND :maxPageId";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("lang", lang.getCode())
            .addValue("minPageId", minPageId)
            .addValue("maxPageId", maxPageId);
        return Objects.requireNonNull(jdbcTemplate.query(sql, namedParameters, new PageResultExtractor()));
    }

    @Override
    public void updatePages(Collection<PageModel> pages) {
        String sql = "UPDATE page SET title = :title WHERE lang = :lang AND article_id = :pageId";
        SqlParameterSource[] namedParameters = SqlParameterSourceUtils.createBatch(pages.toArray());
        jdbcTemplate.batchUpdate(sql, namedParameters);
    }

    @Override
    public void insertPages(Collection<PageModel> pages) {
        String sql = "INSERT INTO page (lang, article_id, title) VALUES (:lang, :pageId, :title)";
        SqlParameterSource[] namedParameters = SqlParameterSourceUtils.createBatch(pages.toArray());
        jdbcTemplate.batchUpdate(sql, namedParameters);
    }

    @Override
    public void deletePages(Collection<PageModel> pages) {
        SqlParameterSource[] namedParameters = SqlParameterSourceUtils.createBatch(pages.toArray());

        // First delete the replacements
        String sqlReplacements = "DELETE FROM replacement WHERE lang = :lang AND article_id = :pageId";
        jdbcTemplate.batchUpdate(sqlReplacements, namedParameters);

        String sqlPages = "DELETE FROM page WHERE lang = :lang AND article_id = :pageId";
        jdbcTemplate.batchUpdate(sqlPages, namedParameters);
    }

    @Override
    public Collection<Integer> findToReview(WikipediaLanguage lang, int numResults) {
        // Find a random page without filtering by type takes a lot
        // Instead we find a random replacement and then the following pages
        long randomStart = findReplacementToReview(lang, numResults);

        // Not worth to DISTINCT. Instead, we return the results as a set to avoid duplicates.
        String sql =
            "SELECT article_id FROM replacement " +
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
            .collect(Collectors.toUnmodifiableSet());
    }

    private long findReplacementToReview(WikipediaLanguage lang, long chunkSize) {
        String sql =
            "SELECT FLOOR(MIN(id) + (MAX(id) - MIN(id) + 1 - :chunkSize) * RAND()) FROM replacement " +
            "WHERE lang = :lang AND reviewer IS NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("chunkSize", chunkSize)
            .addValue("lang", lang.getCode());
        Long result = jdbcTemplate.queryForObject(sql, namedParameters, Long.class);
        return result == null ? 0L : result;
    }

    @Override
    public long countToReview(WikipediaLanguage lang) {
        // FIXME: This should be returning the count of pages to review and not the count of replacements
        // To check how this would affect to performance
        String sql = "SELECT COUNT(*) FROM replacement WHERE lang = :lang AND reviewer IS NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("lang", lang.getCode());
        Long result = jdbcTemplate.queryForObject(sql, namedParameters, Long.class);
        return result == null ? 0L : result;
    }

    @Override
    public Collection<Integer> findToReviewByType(WikipediaLanguage lang, String type, String subtype, int numResults) {
        // When filtering by type/subtype ORDER BY RAND() still takes a while, but it is admissible.
        // Not worth to DISTINCT. Instead, we return the results as a set to avoid duplicates.
        String sql =
            "SELECT article_id FROM replacement " +
            "WHERE lang = :lang AND type = :type AND subtype = :subtype AND reviewer IS NULL " +
            "ORDER BY RAND() " +
            "LIMIT " +
            numResults;
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("lang", lang.getCode())
            .addValue("type", type)
            .addValue("subtype", subtype);
        return jdbcTemplate
            .queryForList(sql, namedParameters, Integer.class)
            .stream()
            .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public long countToReviewByType(WikipediaLanguage lang, String type, String subtype) {
        String sql =
            "SELECT COUNT (DISTINCT article_id) FROM replacement " +
            "WHERE lang = :lang AND type = :type AND subtype = :subtype AND reviewer IS NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("lang", lang.getCode())
            .addValue("type", type)
            .addValue("subtype", subtype);
        Long result = jdbcTemplate.queryForObject(sql, namedParameters, Long.class);
        return result == null ? 0L : result;
    }
}
