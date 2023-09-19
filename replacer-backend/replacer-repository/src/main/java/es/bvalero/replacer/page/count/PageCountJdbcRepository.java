package es.bvalero.replacer.page.count;

import es.bvalero.replacer.common.domain.ResultCount;
import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Collection;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@SuppressWarnings("java:S1192")
@Qualifier("pageCountJdbcRepository")
@Transactional
@Repository
class PageCountJdbcRepository implements PageCountRepository {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public int countNotReviewedByType(WikipediaLanguage lang, @Nullable StandardType type) {
        // This approach is slightly better than using a JOIN with the page table
        String sql = "SELECT COUNT (DISTINCT page_id) FROM replacement WHERE lang = :lang AND reviewer IS NULL";
        MapSqlParameterSource namedParameters = new MapSqlParameterSource().addValue("lang", lang.getCode());
        if (type != null) {
            sql += " AND kind = :kind AND subtype = :subtype";
            namedParameters.addValue("kind", type.getKind().getCode()).addValue("subtype", type.getSubtype());
        }
        Integer result = jdbcTemplate.queryForObject(sql, namedParameters, Integer.class);
        return Objects.requireNonNullElse(result, 0);
    }

    @Override
    public void remove(WikipediaLanguage lang, StandardType type) {
        // Not implemented
    }

    @Override
    public void increment(WikipediaLanguage lang, StandardType type) {
        // Not implemented
    }

    @Override
    public void decrement(WikipediaLanguage lang, StandardType type) {
        // Not implemented
    }

    @Override
    public Collection<ResultCount<StandardType>> countNotReviewedGroupedByType(WikipediaLanguage lang) {
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
                    StandardType.of(resultSet.getByte("KIND"), resultSet.getString("SUBTYPE")),
                    resultSet.getInt("NUM")
                )
        );
    }
}
