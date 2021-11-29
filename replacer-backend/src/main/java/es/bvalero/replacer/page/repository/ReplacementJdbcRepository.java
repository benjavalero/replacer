package es.bvalero.replacer.page.repository;

import com.jcabi.aspects.Loggable;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Loggable(Loggable.TRACE) // To warn about performance issues
@Transactional
@Repository
class ReplacementJdbcRepository implements ReplacementRepository {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public void insertReplacements(Collection<ReplacementModel> replacements) {
        String sql =
            "INSERT INTO replacement (article_id, lang, type, subtype, position, context, last_update, reviewer) " +
            "VALUES (:pageId, :lang, :type, :subtype, :position, :context, :lastUpdate, :reviewer)";
        SqlParameterSource[] namedParameters = SqlParameterSourceUtils.createBatch(replacements.toArray());
        jdbcTemplate.batchUpdate(sql, namedParameters);
    }

    @Override
    public void updateReplacements(Collection<ReplacementModel> replacements) {
        String sql =
            "UPDATE replacement SET position = :position, context = :context, last_update = :lastUpdate WHERE id = :id";
        SqlParameterSource[] namedParameters = SqlParameterSourceUtils.createBatch(replacements.toArray());
        jdbcTemplate.batchUpdate(sql, namedParameters);
    }

    @Override
    public void deleteReplacements(Collection<ReplacementModel> replacements) {
        String sql = "DELETE FROM replacement WHERE id IN (:ids)";
        Set<Long> ids = replacements.stream().map(ReplacementModel::getId).collect(Collectors.toSet());
        assert ids.stream().allMatch(Objects::nonNull);
        SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("ids", ids);
        jdbcTemplate.update(sql, namedParameters);
    }
}