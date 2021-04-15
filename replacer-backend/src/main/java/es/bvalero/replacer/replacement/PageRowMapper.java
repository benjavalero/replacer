package es.bvalero.replacer.replacement;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

public class PageRowMapper implements RowMapper<PageEntity> {

    @Override
    public PageEntity mapRow(ResultSet result, int rowNum) throws SQLException {
        return PageEntity.of(result.getString("LANG"), result.getInt("ARTICLE_ID"), result.getString("TITLE"));
    }
}
