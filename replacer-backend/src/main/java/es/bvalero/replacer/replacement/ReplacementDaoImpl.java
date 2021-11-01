package es.bvalero.replacer.replacement;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.domain.WikipediaLanguage;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Loggable(Loggable.TRACE) // To warn about performance issues
@Repository
@Transactional
class ReplacementDaoImpl implements ReplacementDao, ReplacementStatsDao {

    private static final String FROM_REPLACEMENT_JOIN_PAGE =
        "FROM replacement r JOIN page p ON r.lang = p.lang AND r.article_id = p.article_id ";
    private static final String PARAM_PAGE_ID = "pageId";
    private static final String PARAM_LANG = "lang";
    private static final String PARAM_TYPE = "type";
    private static final String PARAM_SUBTYPE = "subtype";
    private static final String PARAM_REVIEWER = "reviewer";
    private static final String PARAM_VALUE_SYSTEM = "system";

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private PageDao pageDao;

    ///// CRUD

    @Override
    public List<ReplacementEntity> findByPageId(int pageId, WikipediaLanguage lang) {
        String sql =
            "SELECT r.id, r.article_id, r.lang, r.type, r.subtype, r.position, r.context, r.last_update, r.reviewer, p.title " +
            FROM_REPLACEMENT_JOIN_PAGE +
            "WHERE r.lang = :lang AND r.article_id = :pageId";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue(PARAM_LANG, lang.getCode())
            .addValue(PARAM_PAGE_ID, pageId);
        return jdbcTemplate.query(sql, namedParameters, new ReplacementRowMapper());
    }

    @Override
    public void insert(List<ReplacementEntity> entityList) {
        this.insertUpdatePages(entityList);

        final String sql =
            "INSERT INTO replacement (article_id, lang, type, subtype, position, context, last_update, reviewer) " +
            "VALUES (:pageId, :lang, :type, :subtype, :position, :context, :lastUpdate, :reviewer)";
        SqlParameterSource[] namedParameters = SqlParameterSourceUtils.createBatch(entityList.toArray());
        jdbcTemplate.batchUpdate(sql, namedParameters);
    }

    private void insertUpdatePages(List<ReplacementEntity> entityList) {
        entityList
            .stream()
            .map(r -> PageEntity.of(r.getLang(), r.getPageId(), r.getTitle()))
            .distinct()
            .forEach(p -> pageDao.insertUpdatePage(p));
    }

    @Override
    public void update(ReplacementEntity entity) {
        final String sql =
            "UPDATE replacement " +
            "SET position=:position, context=:context, last_update=:lastUpdate " +
            "WHERE id=:id";
        SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(entity);
        jdbcTemplate.update(sql, namedParameters);
    }

    @Override
    public void update(List<ReplacementEntity> entityList) {
        this.insertUpdatePages(entityList);

        final String sql =
            "UPDATE replacement " +
            "SET position=:position, context=:context, last_update=:lastUpdate " +
            "WHERE id=:id";
        SqlParameterSource[] namedParameters = SqlParameterSourceUtils.createBatch(entityList.toArray());
        jdbcTemplate.batchUpdate(sql, namedParameters);
    }

    @Override
    public void updateDate(List<ReplacementEntity> entityList) {
        final String sql = "UPDATE replacement SET last_update=:lastUpdate WHERE id=:id";
        SqlParameterSource[] namedParameters = SqlParameterSourceUtils.createBatch(entityList.toArray());
        jdbcTemplate.batchUpdate(sql, namedParameters);
    }

    @Override
    public void delete(List<ReplacementEntity> entityList) {
        String sql = "DELETE FROM replacement WHERE id IN (:ids)";
        Set<Long> ids = entityList.stream().map(ReplacementEntity::getId).collect(Collectors.toSet());
        SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("ids", ids);
        jdbcTemplate.update(sql, namedParameters);
    }

    ///// DUMP INDEXATION

    @Override
    public List<ReplacementEntity> findByPageInterval(int minPageId, int maxPageId, WikipediaLanguage lang) {
        String sql =
            "SELECT r.id, r.article_id, r.lang, r.type, r.subtype, r.position, r.context, r.last_update, r.reviewer, p.title " +
            FROM_REPLACEMENT_JOIN_PAGE +
            "WHERE r.lang = :lang AND r.article_id BETWEEN :minPageId AND :maxPageId";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue(PARAM_LANG, lang.getCode())
            .addValue("minPageId", minPageId)
            .addValue("maxPageId", maxPageId);
        return jdbcTemplate.query(sql, namedParameters, new ReplacementRowMapper());
    }

    ///// PAGE REVIEW

    @Override
    public long findRandomIdToBeReviewed(WikipediaLanguage lang, long chunkSize) {
        String sql =
            "SELECT FLOOR(MIN(id) + (MAX(id) - MIN(id) + 1 - :chunkSize) * RAND()) FROM replacement " +
            "WHERE lang = :lang AND reviewer IS NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("chunkSize", chunkSize)
            .addValue(PARAM_LANG, lang.getCode());
        Long result = jdbcTemplate.queryForObject(sql, namedParameters, Long.class);
        return result == null ? 0L : result;
    }

    // Not worth to DISTINCT as we add the results as a set later
    @Override
    public List<Integer> findPageIdsToBeReviewed(WikipediaLanguage lang, long start, Pageable pageable) {
        String sql =
            "SELECT article_id FROM replacement " +
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
    @Override
    public List<Integer> findRandomPageIdsToBeReviewedBySubtype(
        WikipediaLanguage lang,
        String type,
        String subtype,
        Pageable pageable
    ) {
        String sql =
            "SELECT article_id FROM replacement " +
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

    @Override
    public long countPagesToBeReviewedBySubtype(WikipediaLanguage lang, String type, String subtype) {
        String sql =
            "SELECT COUNT (DISTINCT article_id) FROM replacement " +
            "WHERE lang = :lang AND type = :type AND subtype = :subtype AND reviewer IS NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue(PARAM_LANG, lang.getCode())
            .addValue(PARAM_TYPE, type)
            .addValue(PARAM_SUBTYPE, subtype);
        Long result = jdbcTemplate.queryForObject(sql, namedParameters, Long.class);
        return result == null ? 0L : result;
    }

    @Override
    public void reviewByPageId(
        WikipediaLanguage lang,
        int pageId,
        @Nullable String type,
        @Nullable String subtype,
        String reviewer
    ) {
        String sql =
            "UPDATE replacement SET reviewer=:reviewer, last_update=:now " +
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

    @Override
    public long countReplacementsReviewed(WikipediaLanguage lang) {
        String sql =
            "SELECT COUNT(*) FROM replacement WHERE lang = :lang AND reviewer IS NOT NULL AND reviewer <> :system";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue(PARAM_LANG, lang.getCode())
            .addValue(PARAM_VALUE_SYSTEM, ReplacementEntity.REVIEWER_SYSTEM);
        Long result = jdbcTemplate.queryForObject(sql, namedParameters, Long.class);
        return result == null ? 0L : result;
    }

    // This count is also used to guess the total for the review without type. Not worth to DISTINCT.
    @Override
    public long countReplacementsNotReviewed(WikipediaLanguage lang) {
        String sql = "SELECT COUNT(*) FROM replacement WHERE lang = :lang AND reviewer IS NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource().addValue(PARAM_LANG, lang.getCode());
        Long result = jdbcTemplate.queryForObject(sql, namedParameters, Long.class);
        return result == null ? 0L : result;
    }

    @Override
    public List<ReviewerCount> countReplacementsGroupedByReviewer(WikipediaLanguage lang) {
        String sql =
            "SELECT reviewer, COUNT(*) AS num FROM replacement " +
            "WHERE lang = :lang AND reviewer IS NOT NULL AND reviewer <> :system " +
            "GROUP BY reviewer " +
            "ORDER BY COUNT(*) DESC";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue(PARAM_LANG, lang.getCode())
            .addValue(PARAM_VALUE_SYSTEM, ReplacementEntity.REVIEWER_SYSTEM);
        return jdbcTemplate.query(sql, namedParameters, new ReviewerCountRowMapper());
    }

    @Override
    @Loggable(value = Loggable.TRACE, limit = 10, unit = TimeUnit.SECONDS)
    public LanguageCount countReplacementsGroupedByType(WikipediaLanguage lang) {
        return LanguageCount.build(countPagesGroupedByTypeAndSubtype(lang));
    }

    private List<TypeSubtypeCount> countPagesGroupedByTypeAndSubtype(WikipediaLanguage lang) {
        String sql =
            "SELECT type, subtype, COUNT(DISTINCT article_id) AS num FROM replacement " +
            "WHERE lang = :lang AND reviewer IS NULL " +
            "GROUP BY lang, type, subtype";
        SqlParameterSource namedParameters = new MapSqlParameterSource().addValue(PARAM_LANG, lang.getCode());
        return jdbcTemplate.query(sql, namedParameters, new TypeSubtypeCountRowMapper());
    }

    ///// PAGE LISTS

    @Override
    public List<String> findPageTitlesToReviewBySubtype(WikipediaLanguage lang, String type, String subtype) {
        String sql =
            "SELECT DISTINCT(p.title) " +
            FROM_REPLACEMENT_JOIN_PAGE +
            "WHERE r.lang = :lang AND r.type = :type AND r.subtype = :subtype AND r.reviewer IS NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue(PARAM_LANG, lang.getCode())
            .addValue(PARAM_TYPE, type)
            .addValue(PARAM_SUBTYPE, subtype);
        return jdbcTemplate.queryForList(sql, namedParameters, String.class);
    }

    @Override
    public void reviewAsSystemBySubtype(WikipediaLanguage lang, String type, String subtype) {
        String sql =
            "UPDATE replacement SET reviewer=:system, last_update=:now " +
            "WHERE lang = :lang AND type = :type AND subtype = :subtype AND reviewer IS NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue(PARAM_VALUE_SYSTEM, ReplacementEntity.REVIEWER_SYSTEM)
            .addValue("now", LocalDate.now())
            .addValue(PARAM_LANG, lang.getCode())
            .addValue(PARAM_TYPE, type)
            .addValue(PARAM_SUBTYPE, subtype);
        jdbcTemplate.update(sql, namedParameters);
    }

    ///// MISSPELLING MANAGER

    @Override
    public void deleteToBeReviewedBySubtype(WikipediaLanguage lang, String type, Set<String> subtypes) {
        String sql =
            "DELETE FROM replacement " +
            "WHERE lang = :lang AND type = :type AND subtype IN (:subtypes) AND reviewer IS NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue(PARAM_LANG, lang.getCode())
            .addValue(PARAM_TYPE, type)
            .addValue("subtypes", subtypes);
        jdbcTemplate.update(sql, namedParameters);
    }
}
