package es.bvalero.replacer.replacement;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

class ReviewerCountRowMapper implements RowMapper<ReviewerCount> {

    @Override
    public ReviewerCount mapRow(ResultSet result, int rowNum) throws SQLException {
        return new ReviewerCount(result.getString("REVIEWER"), result.getLong("NUM"));
    }
}
