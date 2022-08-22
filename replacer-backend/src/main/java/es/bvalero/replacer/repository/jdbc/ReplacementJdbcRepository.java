package es.bvalero.replacer.repository.jdbc;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaPageId;
import es.bvalero.replacer.repository.*;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.logging.LogLevel;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Loggable(value = LogLevel.TRACE, skipResult = true, warnOver = 10, warnUnit = TimeUnit.SECONDS)
@Qualifier("replacementJdbcRepository")
@Transactional
@Repository
class ReplacementJdbcRepository
    implements ReplacementRepository, ReplacementTypeRepository, ReplacementCountRepository {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public void addReplacements(Collection<ReplacementModel> replacements) {
        String sql =
            "INSERT INTO replacement (page_id, lang, kind, subtype, start, context, reviewer) " +
            "VALUES (:pageId, :lang, :kind, :subtype, :start, :context, :reviewer)";
        SqlParameterSource[] namedParameters = SqlParameterSourceUtils.createBatch(replacements.toArray());
        jdbcTemplate.batchUpdate(sql, namedParameters);
    }

    @Override
    public void updateReplacements(Collection<ReplacementModel> replacements) {
        String sql = "UPDATE replacement SET start = :start, context = :context WHERE id = :id";
        SqlParameterSource[] namedParameters = SqlParameterSourceUtils.createBatch(replacements.toArray());
        jdbcTemplate.batchUpdate(sql, namedParameters);
    }

    @Override
    public void removeReplacements(Collection<ReplacementModel> replacements) {
        String sql = "DELETE FROM replacement WHERE id IN (:ids)";
        Set<Integer> ids = replacements
            .stream()
            .map(r -> Objects.requireNonNull(r.getId()))
            .collect(Collectors.toUnmodifiableSet());
        if (!ids.isEmpty()) {
            SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("ids", ids);
            jdbcTemplate.update(sql, namedParameters);
        }
    }

    @Override
    public int countReplacementsReviewed(WikipediaLanguage lang) {
        String sqlReplacement =
            "SELECT COUNT(*) AS num FROM replacement WHERE lang = :lang AND reviewer IS NOT NULL AND reviewer <> :system";
        String sqlCustom = "SELECT COUNT(*) AS num FROM custom WHERE lang = :lang";
        String sqlUnion = String.format("(%s UNION %s) AS sqlUnion", sqlReplacement, sqlCustom);
        String sql = String.format("SELECT SUM(num) FROM %s GROUP BY NULL", sqlUnion);

        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("lang", lang.getCode())
            .addValue("system", REVIEWER_SYSTEM);
        Integer result = jdbcTemplate.queryForObject(sql, namedParameters, Integer.class);
        return Objects.requireNonNullElse(result, 0);
    }

    @Override
    public int countReplacementsNotReviewed(WikipediaLanguage lang) {
        String sql = "SELECT COUNT(*) FROM replacement WHERE lang = :lang AND reviewer IS NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("lang", lang.getCode());
        Integer result = jdbcTemplate.queryForObject(sql, namedParameters, Integer.class);
        return Objects.requireNonNullElse(result, 0);
    }

    @Override
    public Collection<ResultCount<String>> countReplacementsByReviewer(WikipediaLanguage lang) {
        String sqlReplacement =
            "SELECT reviewer, COUNT(*) AS num FROM replacement " +
            "WHERE lang = :lang AND reviewer IS NOT NULL AND reviewer <> :system " +
            "GROUP BY reviewer";
        String sqlCustom = "SELECT reviewer, COUNT(*) AS num FROM custom WHERE lang = :lang GROUP BY reviewer";
        String sqlUnion = String.format("(%s UNION %s) AS sqlUnion", sqlReplacement, sqlCustom);
        String sql =
            String.format("SELECT reviewer, SUM(num) AS numSum FROM %s ", sqlUnion) +
            "GROUP BY reviewer ORDER BY SUM(num) DESC";

        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("lang", lang.getCode())
            .addValue("system", REVIEWER_SYSTEM);
        return Objects.requireNonNull(
            jdbcTemplate.query(
                sql,
                namedParameters,
                (resultSet, rowNum) -> ResultCount.of(resultSet.getString("REVIEWER"), resultSet.getInt("NUMSUM"))
            )
        );
    }

    @Loggable(value = LogLevel.TRACE, skipResult = true, warnOver = 20, warnUnit = TimeUnit.SECONDS)
    @Override
    public Collection<ResultCount<ReplacementType>> countReplacementsByType(WikipediaLanguage lang) {
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
                    ReplacementType.of(resultSet.getByte("KIND"), resultSet.getString("SUBTYPE")),
                    resultSet.getInt("NUM")
                )
        );
    }

    @Override
    public Collection<ResultCount<PageModel>> countReplacementsByPage(WikipediaLanguage lang, int numResults) {
        String sql =
            "SELECT p.page_id, p.title, COUNT(*) AS num " +
            "FROM replacement r JOIN page p ON p.lang = r.lang AND p.page_id = r.page_id " +
            "WHERE r.lang = :lang AND r.reviewer IS NULL " +
            "GROUP BY p.page_id, p.title " +
            "ORDER BY num DESC " +
            "LIMIT " +
            numResults;
        SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("lang", lang.getCode());
        return Objects.requireNonNull(
            jdbcTemplate.query(
                sql,
                namedParameters,
                (resultSet, rowNum) ->
                    ResultCount.of(
                        PageModel
                            .builder()
                            .lang(lang.getCode())
                            .pageId(resultSet.getInt("PAGE_ID"))
                            .title(resultSet.getString("TITLE"))
                            .lastUpdate(LocalDate.now())
                            .build(),
                        resultSet.getInt("NUM")
                    )
            )
        );
    }

    @Override
    public void updateReviewerByType(WikipediaLanguage lang, ReplacementType type, String reviewer) {
        String sql =
            "UPDATE replacement SET reviewer=:reviewer " +
            "WHERE lang = :lang AND kind = :kind AND subtype = :subtype AND reviewer IS NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("reviewer", reviewer)
            .addValue("lang", lang.getCode())
            .addValue("kind", type.getKind().getCode())
            .addValue("subtype", type.getSubtype());
        jdbcTemplate.update(sql, namedParameters);
    }

    @Override
    public void updateReviewerByPageAndType(
        WikipediaPageId wikipediaPageId,
        @Nullable ReplacementType type,
        String reviewer
    ) {
        String from = "UPDATE replacement SET reviewer=:reviewer ";
        String where = "WHERE lang = :lang AND page_id = :pageId AND reviewer IS NULL ";
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("reviewer", reviewer)
            .addValue("lang", wikipediaPageId.getLang().getCode())
            .addValue("pageId", wikipediaPageId.getPageId());

        if (Objects.nonNull(type)) {
            where += "AND kind = :kind AND subtype = :subtype";
            namedParameters =
                namedParameters.addValue("kind", type.getKind().getCode()).addValue("subtype", type.getSubtype());
        }
        String sql = from + where;
        jdbcTemplate.update(sql, namedParameters);
    }

    @Override
    public void updateReviewer(ReplacementModel replacement) {
        String sql =
            "UPDATE replacement SET reviewer = :reviewer " +
            "WHERE lang = :lang AND page_id = :pageId AND kind = :kind AND subtype = :subtype " +
            "AND start = :start AND reviewer IS NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("reviewer", replacement.getReviewer())
            .addValue("lang", replacement.getLang())
            .addValue("pageId", replacement.getPageId())
            .addValue("kind", replacement.getKind())
            .addValue("subtype", replacement.getSubtype())
            .addValue("start", replacement.getStart());
        jdbcTemplate.update(sql, namedParameters);
    }

    @Override
    public void removeReplacementsByType(WikipediaLanguage lang, Collection<ReplacementType> types) {
        if (types.isEmpty() || validateTypesSameKind(types)) {
            throw new IllegalArgumentException();
        } else {
            String sql =
                "DELETE FROM replacement " +
                "WHERE lang = :lang AND kind = :kind AND subtype IN (:subtypes) AND reviewer IS NULL";
            ReplacementKind kind = types.stream().findAny().orElseThrow(IllegalStateException::new).getKind();
            Collection<String> subtypes = types
                .stream()
                .map(ReplacementType::getSubtype)
                .collect(Collectors.toUnmodifiableSet());
            SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("lang", lang.getCode())
                .addValue("kind", kind.getCode())
                .addValue("subtypes", subtypes);
            jdbcTemplate.update(sql, namedParameters);
        }
    }

    private boolean validateTypesSameKind(Collection<ReplacementType> types) {
        return types.stream().map(ReplacementType::getKind).distinct().count() == 1;
    }

    @Override
    public int findReplacementToReview(WikipediaLanguage lang, int chunkSize) {
        String sql =
            "SELECT FLOOR(MIN(id) + (MAX(id) - MIN(id) + 1 - :chunkSize) * RAND()) FROM replacement " +
            "WHERE lang = :lang AND reviewer IS NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("chunkSize", chunkSize)
            .addValue("lang", lang.getCode());
        Integer result = jdbcTemplate.queryForObject(sql, namedParameters, Integer.class);
        return Objects.requireNonNullElse(result, 0);
    }
}
