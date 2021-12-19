package es.bvalero.replacer.repository.jdbc;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaPageId;
import es.bvalero.replacer.repository.*;
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

@Loggable(value = LogLevel.TRACE, skipResult = true, warnOver = 10, warnUnit = TimeUnit.SECONDS)
@Primary
@Qualifier("pageJdbcRepository")
@Transactional
@Repository
class PageJdbcRepository implements PageRepository, PageIndexRepository {

    private static final String FROM_REPLACEMENT_JOIN_PAGE =
        "FROM page p JOIN replacement r ON p.lang = r.lang AND p.article_id = r.article_id ";

    @Autowired
    private ReplacementRepository replacementRepository;

    @Autowired
    private CustomRepository customRepository;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public Optional<PageModel> findPageById(WikipediaPageId id) {
        String sql =
            "SELECT p.lang, p.article_id, p.title, p.last_update, " +
            "r.id, r.type, r.subtype, r.position, r.context, r.reviewer " +
            FROM_REPLACEMENT_JOIN_PAGE +
            "WHERE p.lang = :lang AND p.article_id = :pageId";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("lang", id.getLang().getCode())
            .addValue("pageId", id.getPageId());
        Collection<PageModel> pages = jdbcTemplate.query(sql, namedParameters, new PageResultExtractor());
        return Objects.requireNonNull(pages).stream().findAny();
    }

    @Override
    public Collection<PageModel> findPagesByIdInterval(WikipediaLanguage lang, int minPageId, int maxPageId) {
        String sql =
            "SELECT p.lang, p.article_id, p.title, p.last_update, " +
            "r.id, r.type, r.subtype, r.position, r.context, r.reviewer " +
            FROM_REPLACEMENT_JOIN_PAGE +
            "WHERE p.lang = :lang AND p.article_id BETWEEN :minPageId AND :maxPageId";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("lang", lang.getCode())
            .addValue("minPageId", minPageId)
            .addValue("maxPageId", maxPageId);
        return Objects.requireNonNull(jdbcTemplate.query(sql, namedParameters, new PageResultExtractor()));
    }

    @Override
    public void addPages(Collection<PageModel> pages) {
        String sql =
            "INSERT INTO page (lang, article_id, title, last_update) VALUES (:lang, :pageId, :title, :lastUpdate)";
        SqlParameterSource[] namedParameters = SqlParameterSourceUtils.createBatch(pages.toArray());
        jdbcTemplate.batchUpdate(sql, namedParameters);
    }

    @Override
    public void updatePages(Collection<PageModel> pages) {
        String sql =
            "UPDATE page SET title = :title, last_update = :lastUpdate " +
            "WHERE lang = :lang AND article_id = :pageId";
        SqlParameterSource[] namedParameters = SqlParameterSourceUtils.createBatch(pages.toArray());
        jdbcTemplate.batchUpdate(sql, namedParameters);
    }

    @Override
    public void removePagesById(Collection<WikipediaPageId> wikipediaPageIds) {
        // First delete the replacements
        replacementRepository.removeReplacementsByPageId(wikipediaPageIds);
        customRepository.removeCustomReplacementsByPageId(wikipediaPageIds);

        Collection<PageId> pageIds = wikipediaPageIds.stream().map(PageId::of).collect(Collectors.toUnmodifiableSet());
        SqlParameterSource[] namedParameters = SqlParameterSourceUtils.createBatch(pageIds.toArray());
        String sqlPages = "DELETE FROM page WHERE lang = :lang AND article_id = :pageId";
        jdbcTemplate.batchUpdate(sqlPages, namedParameters);
    }

    @Override
    public Collection<Integer> findPageIdsToReview(WikipediaLanguage lang, int numResults) {
        // Find a random page without filtering by type takes a lot
        // Instead we find a random replacement and then the following pages
        long randomStart = replacementRepository.findReplacementToReview(lang, numResults);

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

    @Override
    public long countPagesToReview(WikipediaLanguage lang) {
        // FIXME: This should be returning the count of pages to review and not the count of replacements
        // To check how this would affect to performance
        String sql = "SELECT COUNT(*) FROM replacement WHERE lang = :lang AND reviewer IS NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("lang", lang.getCode());
        Long result = jdbcTemplate.queryForObject(sql, namedParameters, Long.class);
        return Objects.requireNonNullElse(result, 0L);
    }

    @Override
    public Collection<Integer> findPageIdsToReviewByType(WikipediaLanguage lang, ReplacementType type, int numResults) {
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
            .addValue("type", type.getKind().getLabel())
            .addValue("subtype", type.getSubtype());
        return jdbcTemplate
            .queryForList(sql, namedParameters, Integer.class)
            .stream()
            .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public long countPagesToReviewByType(WikipediaLanguage lang, ReplacementType type) {
        String sql =
            "SELECT COUNT (DISTINCT article_id) FROM replacement " +
            "WHERE lang = :lang AND type = :type AND subtype = :subtype AND reviewer IS NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("lang", lang.getCode())
            .addValue("type", type.getKind().getLabel())
            .addValue("subtype", type.getSubtype());
        Long result = jdbcTemplate.queryForObject(sql, namedParameters, Long.class);
        return Objects.requireNonNullElse(result, 0L);
    }

    @Override
    public Collection<String> findPageTitlesToReviewByType(WikipediaLanguage lang, ReplacementType type) {
        // TODO: Check if it is better to make a DISTINCT or retrieve them all and return a Set
        String sql =
            "SELECT DISTINCT(p.title) " +
            FROM_REPLACEMENT_JOIN_PAGE +
            "WHERE p.lang = :lang AND r.type = :type AND r.subtype = :subtype AND r.reviewer IS NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("lang", lang.getCode())
            .addValue("type", type.getKind().getLabel())
            .addValue("subtype", type.getSubtype());
        return jdbcTemplate.queryForList(sql, namedParameters, String.class);
    }
}
