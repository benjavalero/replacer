package es.bvalero.replacer.replacement.count;

import static es.bvalero.replacer.repository.ReplacementRepository.REVIEWER_SYSTEM;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Loggable(Loggable.TRACE) // To warn about performance issues
@Transactional
@Repository
class ReplacementCountJdbcRepository implements ReplacementCountRepository {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

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
        SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("lang", lang.getCode());
        return jdbcTemplate.query(sql, namedParameters, new TypeSubtypeCountRowMapper());
    }

    @Override
    public void reviewAsSystemByType(WikipediaLanguage lang, String type, String subtype) {
        String sql =
            "UPDATE replacement SET reviewer=:system, last_update=:now " +
            "WHERE lang = :lang AND type = :type AND subtype = :subtype AND reviewer IS NULL";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("system", REVIEWER_SYSTEM)
            .addValue("now", LocalDate.now())
            .addValue("lang", lang.getCode())
            .addValue("type", type)
            .addValue("subtype", subtype);
        jdbcTemplate.update(sql, namedParameters);
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
            .addValue("reviewer", reviewer)
            .addValue("now", LocalDate.now())
            .addValue("lang", lang.getCode())
            .addValue("pageId", pageId);
        if (StringUtils.isNotBlank(type) && StringUtils.isNotBlank(subtype)) {
            sql += "AND type = :type AND subtype = :subtype";
            namedParameters = namedParameters.addValue("type", type).addValue("subtype", subtype);
        }
        jdbcTemplate.update(sql, namedParameters);
    }
}
