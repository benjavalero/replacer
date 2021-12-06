package es.bvalero.replacer.replacement.stats;

import es.bvalero.replacer.replacement.stats.ReviewerCount;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

class ReviewerCountRowMapper implements RowMapper<ReviewerCount> {

    @Override
    public ReviewerCount mapRow(ResultSet result, int rowNum) throws SQLException {
        return ReviewerCount.of(result.getString("REVIEWER"), result.getLong("NUM"));
    }
}
