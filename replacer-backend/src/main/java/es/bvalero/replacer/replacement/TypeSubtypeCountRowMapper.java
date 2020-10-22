package es.bvalero.replacer.replacement;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

public class TypeSubtypeCountRowMapper implements RowMapper<TypeSubtypeCount> {

    @Override
    public TypeSubtypeCount mapRow(ResultSet result, int rowNum) throws SQLException {
        return new TypeSubtypeCount(
            result.getString("LANG"),
            result.getString("TYPE"),
            result.getString("SUBTYPE"),
            result.getLong("NUM")
        );
    }
}
