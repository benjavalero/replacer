package es.bvalero.replacer.common;

import java.time.LocalDateTime;
import java.time.Month;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DateUtilsTest {

    @Test
    void testParseWikipediaDate() {
        LocalDateTime expected = LocalDateTime.of(2018, Month.AUGUST, 31, 5, 17, 28);
        Assertions.assertEquals(expected, DateUtils.parseWikipediaTimestamp("2018-08-31T05:17:28Z"));
    }

    @Test
    void testFormatWikipediaDate() {
        String expected = "2018-08-31T05:17:28Z";
        Assertions.assertEquals(
            expected,
            DateUtils.formatWikipediaTimestamp(LocalDateTime.of(2018, Month.AUGUST, 31, 5, 17, 28))
        );
    }
}
