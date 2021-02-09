package es.bvalero.replacer.replacement;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

public class ReplacementRowMapper implements RowMapper<ReplacementEntity> {

    @Override
    public ReplacementEntity mapRow(ResultSet result, int rowNum) throws SQLException {
        return ReplacementEntity
            .builder()
            .id(result.getLong("ID"))
            .pageId(result.getInt("ARTICLE_ID"))
            .lang(result.getString("LANG"))
            .type(result.getString("TYPE"))
            .subtype(result.getString("SUBTYPE"))
            .position(result.getInt("POSITION"))
            .context(result.getString("CONTEXT"))
            .lastUpdate(result.getDate("LAST_UPDATE").toLocalDate())
            .reviewer(result.getString("REVIEWER"))
            .title(result.getString("TITLE"))
            .build();
    }
}
