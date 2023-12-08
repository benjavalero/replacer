package es.bvalero.replacer.replacement;

import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Qualifier("replacementJdbcRepository")
@Transactional
@Repository
class ReplacementJdbcRepository implements ReplacementSaveRepository {

    // Dependency injection
    private final NamedParameterJdbcTemplate jdbcTemplate;

    ReplacementJdbcRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void add(Collection<IndexedReplacement> replacements) {
        String sql =
            "INSERT INTO replacement (page_id, lang, kind, subtype, start, context, reviewer) " +
            "VALUES (:pageKey.pageId, :pageKey.lang.code, :type.kind.code, :type.subtype, :start, :context, :reviewer)";
        SqlParameterSource[] namedParameters = SqlParameterSourceUtils.createBatch(replacements.toArray());
        jdbcTemplate.batchUpdate(sql, namedParameters);
    }

    @Override
    public void update(Collection<IndexedReplacement> replacements) {
        String sql = "UPDATE replacement SET start = :start, context = :context WHERE id = :id";
        SqlParameterSource[] namedParameters = SqlParameterSourceUtils.createBatch(replacements.toArray());
        jdbcTemplate.batchUpdate(sql, namedParameters);
    }

    @Override
    public void remove(Collection<IndexedReplacement> replacements) {
        if (!replacements.isEmpty()) {
            String sql = "DELETE FROM replacement WHERE id IN (:ids)";
            Set<Integer> ids = replacements
                .stream()
                .map(r -> Objects.requireNonNull(r.getId()))
                .collect(Collectors.toUnmodifiableSet());
            SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("ids", ids);
            jdbcTemplate.update(sql, namedParameters);
        }
    }

    @Override
    public void updateReviewerByType(WikipediaLanguage lang, StandardType type, String reviewer) {
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
    public void updateReviewer(Collection<IndexedReplacement> replacements) {
        replacements.forEach(this::updateReviewer);
    }

    private void updateReviewer(IndexedReplacement replacement) {
        String sql =
            "UPDATE replacement SET reviewer = :reviewer " +
            "WHERE lang = :lang AND page_id = :pageId AND kind = :kind AND subtype = :subtype " +
            "AND start = :start AND reviewer IS NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("reviewer", replacement.getReviewer())
            .addValue("lang", replacement.getPageKey().getLang().getCode())
            .addValue("pageId", replacement.getPageKey().getPageId())
            .addValue("kind", replacement.getType().getKind().getCode())
            .addValue("subtype", replacement.getType().getSubtype())
            .addValue("start", replacement.getStart());
        int numRows = jdbcTemplate.update(sql, namedParameters);
        if (numRows != 1) {
            LOGGER.warn("Indexed Replacement reviewer not updated: {}", replacement);
        }
    }

    @Override
    public void removeByType(WikipediaLanguage lang, StandardType type) {
        String sql =
            "DELETE FROM replacement " +
            "WHERE lang = :lang AND kind = :kind AND subtype = :subtype AND reviewer IS NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("lang", lang.getCode())
            .addValue("kind", type.getKind().getCode())
            .addValue("subtype", type.getSubtype());
        jdbcTemplate.update(sql, namedParameters);
    }
}
