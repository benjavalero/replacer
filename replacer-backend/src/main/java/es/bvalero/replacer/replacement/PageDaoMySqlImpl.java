package es.bvalero.replacer.replacement;

import com.jcabi.aspects.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Profile({ "default", "db-prod" })
@Loggable(Loggable.TRACE) // To warn about performance issues
@Repository
@Transactional
class PageDaoMySqlImpl implements PageDao {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public void insertUpdatePage(PageEntity page) {
        final String sql = "INSERT IGNORE INTO page (lang, article_id, title) " + "VALUES (:lang, :pageId, :title)";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("lang", page.getLang())
            .addValue("pageId", page.getPageId())
            .addValue("title", page.getTitle());
        jdbcTemplate.update(sql, namedParameters);
    }
}
