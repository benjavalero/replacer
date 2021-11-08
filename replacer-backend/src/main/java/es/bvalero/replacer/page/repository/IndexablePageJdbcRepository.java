package es.bvalero.replacer.page.repository;

import es.bvalero.replacer.domain.WikipediaLanguage;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

@Primary
@Component
@Qualifier("indexablePageJdbcRepository")
class IndexablePageJdbcRepository implements IndexablePageRepository {

    private static final String FROM_REPLACEMENT_JOIN_PAGE =
        "FROM replacement r JOIN page p ON r.lang = p.lang AND r.article_id = p.article_id ";

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public Optional<IndexablePageDB> findByPageId(WikipediaLanguage lang, int pageId) {
        String sql =
            "SELECT r.id, r.lang, r.article_id, r.type, r.subtype, r.position, r.context, r.last_update, r.reviewer, p.title " +
            FROM_REPLACEMENT_JOIN_PAGE +
            "WHERE r.lang = :lang AND r.article_id = :pageId";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("lang", lang.getCode())
            .addValue("pageId", pageId);
        List<IndexablePageDB> indexablePages = jdbcTemplate.query(
            sql,
            namedParameters,
            new IndexablePageResultExtractor()
        );
        assert indexablePages != null;
        if (indexablePages.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(indexablePages.get(0));
        }
    }

    @Override
    public List<IndexablePageDB> findByPageIdInterval(WikipediaLanguage lang, int minPageId, int maxPageId) {
        String sql =
            "SELECT r.id, r.lang, r.article_id, r.type, r.subtype, r.position, r.context, r.last_update, r.reviewer, p.title " +
            FROM_REPLACEMENT_JOIN_PAGE +
            "WHERE r.lang = :lang AND r.article_id BETWEEN :minPageId AND :maxPageId";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("lang", lang.getCode())
            .addValue("minPageId", minPageId)
            .addValue("maxPageId", maxPageId);
        return Objects.requireNonNull(jdbcTemplate.query(sql, namedParameters, new IndexablePageResultExtractor()));
    }

    @Override
    public void resetCache(WikipediaLanguage lang) {
        throw new IllegalCallerException();
    }
}
