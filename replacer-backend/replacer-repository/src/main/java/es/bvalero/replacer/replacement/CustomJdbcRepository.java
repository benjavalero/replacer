package es.bvalero.replacer.replacement;

import es.bvalero.replacer.common.domain.CustomType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.page.PageKey;
import java.util.Collection;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.namedparam.*;
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

    @Override
    public void add(IndexedCustomReplacement customReplacement) {
        final String sql =
            "INSERT INTO custom (lang, page_id, replacement, cs, start, reviewer, review_type, review_timestamp, old_rev_id, new_rev_id) " +
            "VALUES (:pageKey.lang.code, :pageKey.pageId, :type.subtype, :cs, :start, :reviewer, :reviewType.code, :reviewTimestamp, :oldRevId, :newRevId)";
        SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(customReplacement);
        jdbcTemplate.update(sql, namedParameters);
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
