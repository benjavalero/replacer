package es.bvalero.replacer.replacement;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

public class CustomRowMapper implements RowMapper<CustomEntity> {

    @Override
    public CustomEntity mapRow(ResultSet result, int rowNum) throws SQLException {
        return CustomEntity
            .builder()
            .id(result.getLong("ID"))
            .lang(result.getString("LANG"))
            .pageId(result.getInt("ARTICLE_ID"))
            .replacement(result.getString("REPLACEMENT"))
            .cs(result.getShort("CS") == 1)
            .lastUpdate(result.getDate("LAST_UPDATE").toLocalDate())
            .reviewer(result.getString("REVIEWER"))
            .build();
    }
}
