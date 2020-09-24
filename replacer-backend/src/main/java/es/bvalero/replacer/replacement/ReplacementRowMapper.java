package es.bvalero.replacer.replacement;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

public class ReplacementRowMapper implements RowMapper<ReplacementEntity> {

    @Override
    public ReplacementEntity mapRow(ResultSet result, int rowNum) throws SQLException {
        return new ReplacementEntity(
            result.getLong("ID"),
            result.getInt("ARTICLE_ID"),
            result.getString("LANG"),
            result.getString("TYPE"),
            result.getString("SUBTYPE"),
            result.getInt("POSITION"),
            result.getString("CONTEXT"),
            result.getDate("LAST_UPDATE").toLocalDate(),
            result.getString("REVIEWER"),
            result.getString("TITLE")
        );
    }
}
