package es.bvalero.replacer.replacement;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Loggable(Loggable.TRACE) // To warn about performance issues
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

    ///// CRUD

    public void insert(ReplacementEntity entity) {
        final String sql =
            "INSERT INTO replacement2 (article_id, lang, type, subtype, position, context, last_update, reviewer, title) " +
            "VALUES (:pageId, :lang, :type, :subtype, :position, :context, :lastUpdate, :reviewer, :title)";
        SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(entity);
        jdbcTemplate.update(sql, namedParameters);
    }

    public void update(ReplacementEntity entity) {
        final String sql =
            "UPDATE replacement2 " +
            "SET position=:position, context=:context, last_update=:lastUpdate " +
            "WHERE id=:id";
        SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(entity);
        jdbcTemplate.update(sql, namedParameters);
    }

    public void deleteAll(List<ReplacementEntity> entityList) {
        String sql = "DELETE FROM replacement2 WHERE id IN (:ids)";
        Set<Long> ids = entityList.stream().map(ReplacementEntity::getId).collect(Collectors.toSet());
        SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("ids", ids);
        jdbcTemplate.update(sql, namedParameters);
    }

    ///// INDEXATION

    public List<ReplacementEntity> findByPageInterval(int minPageId, int maxPageId, WikipediaLanguage lang) {
        // We need all the fields but the title so we don't select it to improve performance
        // We are not interested in the custom replacements when reindexing
        String sql =
            "SELECT id, article_id, lang, type, subtype, position, context, last_update, reviewer, NULL AS title " +
            "FROM replacement2 WHERE lang = :lang AND article_id BETWEEN :minPageId AND :maxPageId AND type <> :type";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue(PARAM_LANG, lang.getCode())
            .addValue("minPageId", minPageId)
            .addValue("maxPageId", maxPageId)
            .addValue(PARAM_TYPE, ReplacementEntity.TYPE_CUSTOM);
        return jdbcTemplate.query(sql, namedParameters, new ReplacementRowMapper());
    }

    public void deleteObsoleteByPageId(WikipediaLanguage lang, Set<Integer> pageIds) {
        String sql =
            "DELETE FROM replacement2 " +
            "WHERE lang = :lang AND article_id IN (:pageIds) AND (reviewer IS NULL OR reviewer = :system)";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue(PARAM_LANG, lang.getCode())
            .addValue("pageIds", pageIds)
            .addValue(PARAM_SYSTEM, ReplacementEntity.REVIEWER_SYSTEM);
        jdbcTemplate.update(sql, namedParameters);
    }

    public List<ReplacementEntity> findByPageId(int pageId, WikipediaLanguage lang) {
        // We need all the fields but the title so we don't select it to improve performance
        String sql =
            "SELECT id, article_id, lang, type, subtype, position, context, last_update, reviewer, NULL AS title " +
            "FROM replacement2 WHERE lang = :lang AND article_id = :pageId AND type <> :type";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue(PARAM_LANG, lang.getCode())
            .addValue(PARAM_PAGE_ID, pageId)
            .addValue(PARAM_TYPE, ReplacementEntity.TYPE_CUSTOM);
        return jdbcTemplate.query(sql, namedParameters, new ReplacementRowMapper());
    }

    ///// PAGE REVIEW

    public long findRandomIdToBeReviewed(long chunkSize, WikipediaLanguage lang) {
        String sql =
            "SELECT FLOOR(MIN(id) + (MAX(id) - MIN(id) + 1 - :chunkSize) * RAND()) FROM replacement2 " +
            "WHERE lang = :lang AND reviewer IS NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("chunkSize", chunkSize)
            .addValue(PARAM_LANG, lang.getCode());
        Long result = jdbcTemplate.queryForObject(sql, namedParameters, Long.class);
        return result == null ? 0L : result;
    }

    // Not worth to DISTINCT as we add the results as a set later
    public List<Integer> findPageIdsToBeReviewed(WikipediaLanguage lang, long start, Pageable pageable) {
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
            .addValue("start", start);
        return jdbcTemplate.queryForList(sql, namedParameters, Integer.class);
    }

    // When filtering by type/subtype ORDER BY RAND() still takes a while but it is admissible
    // Not worth to DISTINCT as we add the results as a set later
    public List<Integer> findRandomPageIdsToBeReviewedBySubtype(
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

    public long countPagesToBeReviewedBySubtype(WikipediaLanguage lang, String type, String subtype) {
        String sql =
            "SELECT COUNT (DISTINCT article_id) FROM replacement2 " +
            "WHERE lang = :lang AND type = :type AND subtype = :subtype AND reviewer IS NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue(PARAM_LANG, lang.getCode())
            .addValue(PARAM_TYPE, type)
            .addValue(PARAM_SUBTYPE, subtype);
        Long result = jdbcTemplate.queryForObject(sql, namedParameters, Long.class);
        return result == null ? 0L : result;
    }

    // Using DISTINCT makes the query not to use to wanted index "idx_count"
    public List<Integer> findPageIdsReviewedByCustomTypeAndSubtype(WikipediaLanguage lang, String subtype) {
        String sql =
            "SELECT article_id FROM replacement2 " +
            "WHERE lang = :lang AND type = :type AND subtype = :subtype AND reviewer IS NOT NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue(PARAM_LANG, lang.getCode())
            .addValue(PARAM_TYPE, ReplacementEntity.TYPE_CUSTOM)
            .addValue(PARAM_SUBTYPE, subtype);
        return jdbcTemplate.queryForList(sql, namedParameters, Integer.class);
    }

    public void reviewByPageId(
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

    ///// STATISTICS

    public long countReplacementsReviewed(WikipediaLanguage lang) {
        String sql =
            "SELECT COUNT(*) FROM replacement2 " +
            "WHERE lang = :lang AND reviewer IS NOT NULL AND reviewer <> :system";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue(PARAM_LANG, lang.getCode())
            .addValue(PARAM_SYSTEM, ReplacementEntity.REVIEWER_SYSTEM);
        Long result = jdbcTemplate.queryForObject(sql, namedParameters, Long.class);
        return result == null ? 0L : result;
    }

    // This count is also used to guess the total for the review without type. Not worth to DISTINCT.
    public long countReplacementsNotReviewed(WikipediaLanguage lang) {
        String sql = "SELECT COUNT(*) FROM replacement2 WHERE lang = :lang AND reviewer IS NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource().addValue(PARAM_LANG, lang.getCode());
        Long result = jdbcTemplate.queryForObject(sql, namedParameters, Long.class);
        return result == null ? 0L : result;
    }

    public List<ReviewerCount> countReplacementsGroupedByReviewer(WikipediaLanguage lang) {
        String sql =
            "SELECT reviewer, COUNT(*) AS num FROM replacement2 " +
            "WHERE lang = :lang AND reviewer IS NOT NULL AND reviewer <> :system " +
            "GROUP BY reviewer " +
            "ORDER BY COUNT(*) DESC";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue(PARAM_LANG, lang.getCode())
            .addValue(PARAM_SYSTEM, ReplacementEntity.REVIEWER_SYSTEM);
        return jdbcTemplate.query(sql, namedParameters, new ReviewerCountRowMapper());
    }

    public List<TypeSubtypeCount> countPagesGroupedByTypeAndSubtype() {
        String sql =
            "SELECT lang, type, subtype, COUNT(DISTINCT article_id) AS num FROM replacement2 " +
            "WHERE reviewer IS NULL " +
            "GROUP BY lang, type, subtype";
        return jdbcTemplate.query(sql, new TypeSubtypeCountRowMapper());
    }

    ///// PAGE LISTS

    public List<String> findPageTitlesToReviewBySubtype(WikipediaLanguage lang, String type, String subtype) {
        String sql =
            "SELECT DISTINCT(title) FROM replacement2 " +
            "WHERE lang = :lang AND type = :type AND subtype = :subtype AND reviewer IS NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue(PARAM_LANG, lang.getCode())
            .addValue(PARAM_TYPE, type)
            .addValue(PARAM_SUBTYPE, subtype);
        return jdbcTemplate.queryForList(sql, namedParameters, String.class);
    }

    public void reviewAsSystemBySubtype(WikipediaLanguage lang, String type, String subtype) {
        String sql =
            "UPDATE replacement2 SET reviewer=:system, last_update=:now " +
            "WHERE lang = :lang AND type = :type AND subtype = :subtype AND reviewer IS NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue(PARAM_SYSTEM, ReplacementEntity.REVIEWER_SYSTEM)
            .addValue("now", LocalDate.now())
            .addValue(PARAM_LANG, lang.getCode())
            .addValue(PARAM_TYPE, type)
            .addValue(PARAM_SUBTYPE, subtype);
        jdbcTemplate.update(sql, namedParameters);
    }

    ///// OTHER

    public void deleteToBeReviewedBySubtype(WikipediaLanguage lang, String type, Set<String> subtypes) {
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
