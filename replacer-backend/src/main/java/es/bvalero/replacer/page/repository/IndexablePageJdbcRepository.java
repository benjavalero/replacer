package es.bvalero.replacer.page.repository;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.domain.WikipediaLanguage;
import java.util.*;
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
@Qualifier("indexablePageJdbcRepository")
class IndexablePageJdbcRepository implements IndexablePageRepository {

    private static final String FROM_REPLACEMENT_JOIN_PAGE =
        "FROM replacement r JOIN page p ON r.lang = p.lang AND r.article_id = p.article_id ";

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public Optional<IndexablePage> findByPageId(IndexablePageId id) {
        String sql =
            "SELECT r.id, r.lang, r.article_id, r.type, r.subtype, r.position, r.context, r.last_update, r.reviewer, p.title " +
            FROM_REPLACEMENT_JOIN_PAGE +
            "WHERE r.lang = :lang AND r.article_id = :pageId";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("lang", id.getLang().getCode())
            .addValue("pageId", id.getPageId());
        List<IndexablePage> indexablePages = jdbcTemplate.query(
            sql,
            namedParameters,
            new IndexablePageResultExtractor()
        );
        assert indexablePages != null;
        if (indexablePages.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(indexablePages.get(0));
        }
    }

    @Override
    public List<IndexablePage> findByPageIdInterval(WikipediaLanguage lang, int minPageId, int maxPageId) {
        String sql =
            "SELECT r.id, r.lang, r.article_id, r.type, r.subtype, r.position, r.context, r.last_update, r.reviewer, p.title " +
            FROM_REPLACEMENT_JOIN_PAGE +
            "WHERE r.lang = :lang AND r.article_id BETWEEN :minPageId AND :maxPageId";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("lang", lang.getCode())
            .addValue("minPageId", minPageId)
            .addValue("maxPageId", maxPageId);
        return Objects.requireNonNull(jdbcTemplate.query(sql, namedParameters, new IndexablePageResultExtractor()));
    }

    @Override
    public void resetCache() {
        throw new IllegalCallerException();
    }

    @Override
    public void updatePageTitles(Collection<IndexablePage> pages) {
        String sql = "UPDATE page SET title = :title WHERE lang = :lang AND article_id = :pageId";
        SqlParameterSource[] namedParameters = SqlParameterSourceUtils.createBatch(pages.toArray());
        jdbcTemplate.batchUpdate(sql, namedParameters);
    }

    @Override
    public void insertPages(Collection<IndexablePage> pages) {
        String sql = "INSERT INTO page (lang, article_id, title) VALUES (:lang, :pageId, :title)";
        SqlParameterSource[] namedParameters = SqlParameterSourceUtils.createBatch(pages.toArray());
        jdbcTemplate.batchUpdate(sql, namedParameters);
    }

    @Override
    public void deletePages(Collection<IndexablePage> pages) {
        SqlParameterSource[] namedParameters = SqlParameterSourceUtils.createBatch(pages.toArray());

        // First delete the replacements
        String sqlReplacements = "DELETE FROM replacement WHERE lang = :lang AND article_id = :pageId";
        jdbcTemplate.batchUpdate(sqlReplacements, namedParameters);

        String sqlPages = "DELETE FROM page WHERE lang = :lang AND article_id = :pageId";
        jdbcTemplate.batchUpdate(sqlPages, namedParameters);
    }

    @Override
    public void insertReplacements(Collection<IndexableReplacement> replacements) {
        String sql =
            "INSERT INTO replacement (article_id, lang, type, subtype, position, context, last_update, reviewer) " +
            "VALUES (:pageId, :lang, :type, :subtype, :position, :context, :lastUpdate, :reviewer)";
        SqlParameterSource[] namedParameters = SqlParameterSourceUtils.createBatch(replacements.toArray());
        jdbcTemplate.batchUpdate(sql, namedParameters);
    }

    @Override
    public void updateReplacements(Collection<IndexableReplacement> replacements) {
        String sql =
            "UPDATE replacement SET position = :position, context = :context, last_update = :lastUpdate WHERE id = :id";
        SqlParameterSource[] namedParameters = SqlParameterSourceUtils.createBatch(replacements.toArray());
        jdbcTemplate.batchUpdate(sql, namedParameters);
    }

    @Override
    public void deleteReplacements(Collection<IndexableReplacement> replacements) {
        String sql = "DELETE FROM replacement WHERE id IN (:ids)";
        Set<Long> ids = replacements.stream().map(IndexableReplacement::getId).collect(Collectors.toSet());
        assert ids.stream().allMatch(Objects::nonNull);
        SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("ids", ids);
        jdbcTemplate.update(sql, namedParameters);
    }
}
