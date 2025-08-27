package es.bvalero.replacer.replacement;

import es.bvalero.replacer.common.domain.PageKey;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.CustomType;
import es.bvalero.replacer.page.IndexedCustomReplacement;
import java.util.Collection;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Repository
class CustomJdbcRepository implements CustomRepository {

    // Dependency injection
    private final NamedParameterJdbcTemplate jdbcTemplate;

    CustomJdbcRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Using DISTINCT makes the query not to use to wanted index "idx_count"
    @Override
    public Collection<PageKey> findPagesReviewed(WikipediaLanguage lang, CustomType type) {
        String sql = "SELECT page_id FROM custom WHERE lang = :lang AND replacement = :replacement AND cs = :cs";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("lang", lang.getCode())
            .addValue("replacement", type.getSubtype())
            .addValue("cs", IndexedCustomReplacement.getCs(type.isCaseSensitive()));
        return jdbcTemplate
            .queryForList(sql, namedParameters, Integer.class)
            .stream()
            .map(pageId -> PageKey.of(lang, pageId))
            .collect(Collectors.toUnmodifiableSet());
    }
}
