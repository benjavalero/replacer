package es.bvalero.replacer.replacement.count;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

class TypeSubtypeCountRowMapper implements RowMapper<TypeSubtypeCount> {

    @Override
    public TypeSubtypeCount mapRow(ResultSet result, int rowNum) throws SQLException {
        return TypeSubtypeCount.of(result.getString("TYPE"), result.getString("SUBTYPE"), result.getLong("NUM"));
    }
}
