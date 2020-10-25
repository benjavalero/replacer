package es.bvalero.replacer.replacement;

import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class ReplacementDao {
    private static final String PARAM_PAGE_ID = "pageId";
    private static final String PARAM_LANG = "lang";
    private static final String PARAM_TYPE = "type";
    private static final String PARAM_SUBTYPE = "subtype";
    private static final String PARAM_REVIEWER = "reviewer";
    private static final String PARAM_SYSTEM = "system";

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void insert(ReplacementEntity entity) {
        final String sql =
            "INSERT INTO replacement2 (article_id, lang, type, subtype, position, context, last_update, reviewer, title) " +
            "VALUES (:pageId, :lang, :type, :subtype, :position, :context, :lastUpdate, :reviewer, :title)";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue(PARAM_PAGE_ID, entity.getPageId())
            .addValue(PARAM_LANG, entity.getLang())
            .addValue(PARAM_TYPE, entity.getType())
            .addValue(PARAM_SUBTYPE, entity.getSubtype())
            .addValue("position", entity.getPosition())
            .addValue("context", entity.getContext())
            .addValue("lastUpdate", entity.getLastUpdate())
            .addValue(PARAM_REVIEWER, entity.getReviewer())
            .addValue("title", entity.getTitle());
        jdbcTemplate.update(sql, namedParameters);
    }

    public void update(ReplacementEntity entity) {
        final String sql =
            "UPDATE replacement2 " +
            "SET position=:position, context=:context, last_update=:lastUpdate, reviewer=:reviewer, title=:title " +
            "WHERE id=:id";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("id", entity.getId())
            .addValue("position", entity.getPosition())
            .addValue("context", entity.getContext())
            .addValue("lastUpdate", entity.getLastUpdate())
            .addValue(PARAM_REVIEWER, entity.getReviewer())
            .addValue("title", entity.getTitle());
        jdbcTemplate.update(sql, namedParameters);
    }

    public void deleteAll(List<ReplacementEntity> entityList) {
        String sql = "DELETE FROM replacement2 WHERE id IN (:ids)";
        Set<Long> ids = entityList.stream().map(ReplacementEntity::getId).collect(Collectors.toSet());
        SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("ids", ids);
        jdbcTemplate.update(sql, namedParameters);
    }

    public List<ReplacementEntity> findByPages(int minId, int maxId, WikipediaLanguage lang) {
        // We need all the fields but the title so we don't select it to improve performance
        String sql =
            "SELECT id, article_id, lang, type, subtype, position, context, last_update, reviewer, NULL AS title " +
            "FROM replacement2 WHERE lang = :lang AND article_id BETWEEN :minId AND :maxId";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue(PARAM_LANG, lang.getCode())
            .addValue("minId", minId)
            .addValue("maxId", maxId);
        return jdbcTemplate.query(sql, namedParameters, new ReplacementRowMapper());
    }

    public void deleteObsoleteReplacements(WikipediaLanguage lang, Set<Integer> pageIds) {
        String sql =
            "DELETE FROM replacement2 " +
            "WHERE lang = :lang AND article_id IN (:pageIds) AND (reviewer IS NULL OR reviewer = :system)";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue(PARAM_SYSTEM, ReplacementIndexService.SYSTEM_REVIEWER)
            .addValue(PARAM_LANG, lang.getCode())
            .addValue("pageIds", pageIds);
        jdbcTemplate.update(sql, namedParameters);
    }

    public void reviewTypeReplacementsAsSystem(WikipediaLanguage lang, String type, String subtype) {
        String sql =
            "UPDATE replacement2 SET reviewer=:system, last_update=:now " +
            "WHERE lang = :lang AND type = :type AND subtype = :subtype AND reviewer IS NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue(PARAM_SYSTEM, ReplacementIndexService.SYSTEM_REVIEWER)
            .addValue("now", LocalDate.now())
            .addValue(PARAM_LANG, lang.getCode())
            .addValue(PARAM_TYPE, type)
            .addValue(PARAM_SUBTYPE, subtype);
        jdbcTemplate.update(sql, namedParameters);
    }

    public Long findRandomStart(long chunkSize, WikipediaLanguage lang) {
        String sql =
            "SELECT FLOOR(MIN(id) + (MAX(id) - MIN(id) + 1 - :chunkSize) * RAND()) FROM replacement2 " +
            "WHERE lang = :lang AND reviewer IS NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("chunkSize", chunkSize)
            .addValue(PARAM_LANG, lang.getCode());
        return jdbcTemplate.queryForObject(sql, namedParameters, Long.class);
    }

    // ORDER BY RAND() takes a lot when not filtering by type/subtype even using an index
    // Not worth to DISTINCT as we add the results as a set later
    public List<Integer> findRandomPageIdsToReview(WikipediaLanguage lang, long randomStart, Pageable pageable) {
        String sql =
            "SELECT article_id FROM replacement2 " +
            "WHERE lang = :lang AND reviewer IS NULL AND id > :start " +
            "ORDER BY id " +
            "LIMIT " +
            pageable.getPageSize() +
            " " +
            "OFFSET " +
            pageable.getOffset();
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue(PARAM_LANG, lang.getCode())
            .addValue("start", randomStart);
        return jdbcTemplate.queryForList(sql, namedParameters, Integer.class);
    }

    public List<Integer> findByLangAndTypeAndSubtypeAndReviewerNotNull(
        WikipediaLanguage lang,
        String type,
        String subtype
    ) {
        String sql =
            "SELECT article_id FROM replacement2 " +
            "WHERE lang = :lang AND type = :type AND subtype = :subtype AND reviewer IS NOT NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue(PARAM_LANG, lang.getCode())
            .addValue(PARAM_TYPE, type)
            .addValue(PARAM_SUBTYPE, subtype);
        return jdbcTemplate.queryForList(sql, namedParameters, Integer.class);
    }

    // When filtering by type/subtype ORDER BY RAND() still takes a while but it is admissible
    // Not worth to DISTINCT as we add the results as a set later
    public List<Integer> findRandomPageIdsToReviewByTypeAndSubtype(
        WikipediaLanguage lang,
        String type,
        String subtype,
        Pageable pageable
    ) {
        String sql =
            "SELECT article_id FROM replacement2 " +
            "WHERE lang = :lang AND type = :type AND subtype = :subtype AND reviewer IS NULL " +
            "ORDER BY RAND() " +
            "LIMIT " +
            pageable.getPageSize() +
            " " +
            "OFFSET " +
            pageable.getOffset();
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue(PARAM_LANG, lang.getCode())
            .addValue(PARAM_TYPE, type)
            .addValue(PARAM_SUBTYPE, subtype);
        return jdbcTemplate.queryForList(sql, namedParameters, Integer.class);
    }

    public Long countByLangAndTypeAndSubtypeAndReviewerIsNull(WikipediaLanguage lang, String type, String subtype) {
        String sql =
            "SELECT COUNT (DISTINCT article_id) FROM replacement2 " +
            "WHERE lang = :lang AND type = :type AND subtype = :subtype AND reviewer IS NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue(PARAM_LANG, lang.getCode())
            .addValue(PARAM_TYPE, type)
            .addValue(PARAM_SUBTYPE, subtype);
        return jdbcTemplate.queryForObject(sql, namedParameters, Long.class);
    }

    public Long countByLangAndReviewed(WikipediaLanguage lang) {
        String sql =
            "SELECT COUNT(*) FROM replacement2 " +
            "WHERE lang = :lang AND reviewer IS NOT NULL AND reviewer <> :system";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue(PARAM_LANG, lang.getCode())
            .addValue(PARAM_SYSTEM, ReplacementIndexService.SYSTEM_REVIEWER);
        return jdbcTemplate.queryForObject(sql, namedParameters, Long.class);
    }

    // Not worth to DISTINCT. Besides this count is also used in statistics.
    public Long countByLangAndReviewerIsNull(WikipediaLanguage lang) {
        String sql = "SELECT COUNT(*) FROM replacement2 " + "WHERE lang = :lang AND reviewer IS NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource().addValue(PARAM_LANG, lang.getCode());
        return jdbcTemplate.queryForObject(sql, namedParameters, Long.class);
    }

    public List<ReviewerCount> countGroupedByReviewer(WikipediaLanguage lang) {
        String sql =
            "SELECT reviewer, COUNT(*) AS num FROM replacement2 " +
            "WHERE lang = :lang AND reviewer IS NOT NULL AND reviewer <> :system " +
            "GROUP BY reviewer " +
            "ORDER BY COUNT(*) DESC";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue(PARAM_LANG, lang.getCode())
            .addValue(PARAM_SYSTEM, ReplacementIndexService.SYSTEM_REVIEWER);
        return jdbcTemplate.query(sql, namedParameters, new ReviewerCountRowMapper());
    }

    public List<TypeSubtypeCount> countGroupedByTypeAndSubtype() {
        String sql =
            "SELECT lang, type, subtype, COUNT(DISTINCT article_id) AS num FROM replacement2 " +
            "WHERE reviewer IS NULL " +
            "GROUP BY lang, type, subtype";
        return jdbcTemplate.query(sql, new TypeSubtypeCountRowMapper());
    }

    public List<String> findPageTitlesByTypeAndSubtype(WikipediaLanguage lang, String type, String subtype) {
        String sql =
            "SELECT DISTINCT(title) FROM replacement2 " +
            "WHERE lang = :lang AND type = :type AND subtype = :subtype AND reviewer IS NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue(PARAM_LANG, lang.getCode())
            .addValue(PARAM_TYPE, type)
            .addValue(PARAM_SUBTYPE, subtype);
        return jdbcTemplate.queryForList(sql, namedParameters, String.class);
    }

    public List<ReplacementEntity> findByPageIdAndLang(int pageId, WikipediaLanguage lang) {
        // We need all the fields but the title so we don't select it to improve performance
        String sql =
            "SELECT id, article_id, lang, type, subtype, position, context, last_update, reviewer, NULL AS title " +
            "FROM replacement2 WHERE lang = :lang AND article_id = :pageId";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue(PARAM_LANG, lang.getCode())
            .addValue(PARAM_PAGE_ID, pageId);
        return jdbcTemplate.query(sql, namedParameters, new ReplacementRowMapper());
    }

    public void reviewPageReplacements(
        WikipediaLanguage lang,
        int pageId,
        @Nullable String type,
        @Nullable String subtype,
        String reviewer
    ) {
        String sql =
            "UPDATE replacement2 SET reviewer=:reviewer, last_update=:now " +
            "WHERE lang = :lang AND article_id = :pageId AND reviewer IS NULL ";
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue(PARAM_REVIEWER, reviewer)
            .addValue("now", LocalDate.now())
            .addValue(PARAM_LANG, lang.getCode())
            .addValue(PARAM_PAGE_ID, pageId);
        if (StringUtils.isNotBlank(type) && StringUtils.isNotBlank(subtype)) {
            sql += "AND type = :type AND subtype = :subtype";
            namedParameters = namedParameters.addValue(PARAM_TYPE, type).addValue(PARAM_SUBTYPE, subtype);
        }
        jdbcTemplate.update(sql, namedParameters);
    }

    public void deleteByLangAndTypeAndSubtypeInAndReviewerIsNull(
        WikipediaLanguage lang,
        String type,
        Set<String> subtypes
    ) {
        String sql =
            "DELETE FROM replacement2 " +
            "WHERE lang = :lang AND type = :type AND subtype IN (:subtypes) AND reviewer IS NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue(PARAM_LANG, lang.getCode())
            .addValue(PARAM_TYPE, type)
            .addValue("subtypes", subtypes);
        jdbcTemplate.update(sql, namedParameters);
    }
}
