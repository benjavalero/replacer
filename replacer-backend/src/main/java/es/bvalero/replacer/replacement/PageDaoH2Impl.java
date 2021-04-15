package es.bvalero.replacer.replacement;

import com.jcabi.aspects.Loggable;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Profile({ "db-local", "offline" })
@Loggable(Loggable.TRACE) // To warn about performance issues
@Repository
@Transactional
class PageDaoH2Impl implements PageDao {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public void insertUpdatePage(PageEntity page) {
        if (find(page.getLang(), page.getPageId()).isEmpty()) {
            this.insert(page);
        } else {
            this.update(page);
        }
    }

    private List<PageEntity> find(String lang, int pageId) {
        String sql = "SELECT lang, article_id, title FROM page WHERE lang = :lang AND article_id = :pageId";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("lang", lang)
            .addValue("pageId", pageId);
        return jdbcTemplate.query(sql, namedParameters, new PageRowMapper());
    }

    private void insert(PageEntity entity) {
        final String sql = "INSERT INTO page (lang, article_id, title) VALUES (:lang, :pageId, :title)";
        SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(entity);
        jdbcTemplate.update(sql, namedParameters);
    }

    private void update(PageEntity entity) {
        final String sql = "UPDATE page SET title=:title WHERE lang=:lang AND article_id=:pageId";
        SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(entity);
        jdbcTemplate.update(sql, namedParameters);
    }
}
