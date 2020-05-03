package es.bvalero.replacer.replacement;

import es.bvalero.replacer.dump.ReplacementRowMapper;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class ReplacementDao {
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public List<ReplacementEntity> findByArticles(int minId, int maxId, WikipediaLanguage lang) {
        String sql = "SELECT * FROM replacement2 WHERE lang = :lang AND article_id BETWEEN :minId AND :maxId";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("lang", lang.getCode())
            .addValue("minId", minId)
            .addValue("maxId", maxId);
        return jdbcTemplate.query(sql, namedParameters, new ReplacementRowMapper());
    }

    public void reviewArticlesReplacementsAsSystem(Set<Integer> articleIds, WikipediaLanguage lang) {
        String sql =
            "UPDATE replacement2 SET reviewer=:system, last_update=:now " +
            "WHERE lang = :lang AND article_id IN (:articleIds) AND reviewer IS NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("system", ReplacementIndexService.SYSTEM_REVIEWER)
            .addValue("now", LocalDate.now())
            .addValue("lang", lang.getCode())
            .addValue("articleIds", articleIds);
        jdbcTemplate.update(sql, namedParameters);
    }
}
