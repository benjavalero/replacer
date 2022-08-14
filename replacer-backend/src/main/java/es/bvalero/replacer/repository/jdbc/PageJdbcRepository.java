package es.bvalero.replacer.repository.jdbc;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaPageId;
import es.bvalero.replacer.repository.*;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.logging.LogLevel;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@SuppressWarnings("java:S1192")
@Loggable(value = LogLevel.TRACE, skipResult = true, warnOver = 10, warnUnit = TimeUnit.SECONDS)
@Primary
@Qualifier("pageJdbcRepository")
@Transactional
@Repository
class PageJdbcRepository implements PageRepository, PageIndexRepository {

    private static final String FROM_REPLACEMENT_JOIN_PAGE =
        "FROM page p LEFT JOIN replacement r ON p.lang = r.lang AND p.page_id = r.page_id ";

    @Autowired
    private ReplacementRepository replacementRepository;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public Optional<PageModel> findPageById(WikipediaPageId id) {
        String sql =
            "SELECT p.lang, p.page_id, p.title, p.last_update, " +
            "r.id, r.type AS kind, r.subtype, r.position, r.context, r.reviewer " +
            FROM_REPLACEMENT_JOIN_PAGE +
            "WHERE p.lang = :lang AND p.page_id = :pageId";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("lang", id.getLang().getCode())
            .addValue("pageId", id.getPageId());
        Collection<PageModel> pages = jdbcTemplate.query(sql, namedParameters, new PageResultExtractor());
        return Objects.requireNonNull(pages).stream().findAny();
    }

    @Override
    public Collection<PageModel> findPagesByIdInterval(WikipediaLanguage lang, int minPageId, int maxPageId) {
        String sql =
            "SELECT p.lang, p.page_id, p.title, p.last_update, " +
            "r.id, r.type AS kind, r.subtype, r.position, r.context, r.reviewer " +
            FROM_REPLACEMENT_JOIN_PAGE +
            "WHERE p.lang = :lang AND p.page_id BETWEEN :minPageId AND :maxPageId";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("lang", lang.getCode())
            .addValue("minPageId", minPageId)
            .addValue("maxPageId", maxPageId);
        return Objects.requireNonNull(jdbcTemplate.query(sql, namedParameters, new PageResultExtractor()));
    }

    @Override
    public void addPages(Collection<PageModel> pages) {
        String sql =
            "INSERT INTO page (lang, page_id, title, last_update) VALUES (:lang, :pageId, :title, :lastUpdate)";
        SqlParameterSource[] namedParameters = SqlParameterSourceUtils.createBatch(pages.toArray());
        jdbcTemplate.batchUpdate(sql, namedParameters);
    }

    @Override
    public void updatePages(Collection<PageModel> pages) {
        String sql =
            "UPDATE page SET title = :title, last_update = :lastUpdate WHERE lang = :lang AND page_id = :pageId";
        SqlParameterSource[] namedParameters = SqlParameterSourceUtils.createBatch(pages.toArray());
        jdbcTemplate.batchUpdate(sql, namedParameters);
    }

    @Override
    public void updatePageLastUpdate(WikipediaPageId id, LocalDate lastUpdate) {
        String sql = "UPDATE page SET last_update = :now WHERE lang = :lang AND page_id = :pageId";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("now", lastUpdate)
            .addValue("lang", id.getLang().getCode())
            .addValue("pageId", id.getPageId());
        jdbcTemplate.update(sql, namedParameters);
    }

    @Override
    public void removePagesById(Collection<WikipediaPageId> wikipediaPageIds) {
        // No need to delete first the replacements as they are deleted on cascade by the database
        Collection<PageId> pageIds = wikipediaPageIds.stream().map(PageId::of).collect(Collectors.toUnmodifiableSet());
        SqlParameterSource[] namedParameters = SqlParameterSourceUtils.createBatch(pageIds.toArray());
        String sqlPages = "DELETE FROM page WHERE lang = :lang AND page_id = :pageId";
        jdbcTemplate.batchUpdate(sqlPages, namedParameters);
    }

    @Override
    public Collection<Integer> findPageIdsToReview(WikipediaLanguage lang, int numResults) {
        // Find a random page without filtering by type takes a lot
        // Instead we find a random replacement and then the following pages
        int randomStart = replacementRepository.findReplacementToReview(lang, numResults);

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
            .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public int countPagesToReview(WikipediaLanguage lang) {
        // To check how this would affect to performance
        String sql = "SELECT COUNT (DISTINCT page_id) FROM replacement WHERE lang = :lang AND reviewer IS NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("lang", lang.getCode());
        Integer result = jdbcTemplate.queryForObject(sql, namedParameters, Integer.class);
        return Objects.requireNonNullElse(result, 0);
    }

    @Override
    public Collection<Integer> findPageIdsToReviewByType(WikipediaLanguage lang, ReplacementType type, int numResults) {
        // When filtering by type/subtype ORDER BY RAND() still takes a while, but it is admissible.
        // Not worth to DISTINCT. Instead, we return the results as a set to avoid duplicates.
        String sql =
            "SELECT page_id FROM replacement " +
            "WHERE lang = :lang AND type = :kind AND subtype = :subtype AND reviewer IS NULL " +
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
            .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public int countPagesToReviewByType(WikipediaLanguage lang, ReplacementType type) {
        // This approach is slightly better than using a JOIN with the page table
        String sql =
            "SELECT COUNT (DISTINCT page_id) FROM replacement " +
            "WHERE lang = :lang AND type = :kind AND subtype = :subtype AND reviewer IS NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("lang", lang.getCode())
            .addValue("kind", type.getKind().getCode())
            .addValue("subtype", type.getSubtype());
        Integer result = jdbcTemplate.queryForObject(sql, namedParameters, Integer.class);
        return Objects.requireNonNullElse(result, 0);
    }

    @Override
    public Collection<String> findPageTitlesToReviewByType(WikipediaLanguage lang, ReplacementType type) {
        String sql =
            "SELECT p.title FROM page p " +
            "WHERE p.lang = :lang AND EXISTS " +
            "(SELECT NULL FROM replacement r WHERE p.lang = r.lang AND p.page_id = r.page_id " +
            "AND r.type = :kind AND r.subtype = :subtype AND r.reviewer IS NULL)";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("lang", lang.getCode())
            .addValue("kind", type.getKind().getCode())
            .addValue("subtype", type.getSubtype());
        return jdbcTemplate.queryForList(sql, namedParameters, String.class);
    }
}
