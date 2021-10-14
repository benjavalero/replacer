package es.bvalero.replacer.wikipedia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;

class WikipediaDateUtilsTest {

    @Test
    void testParseWikipediaDate() {
        LocalDateTime expected = LocalDateTime.of(2018, Month.AUGUST, 31, 5, 17, 28);
        assertEquals(expected, WikipediaDateUtils.parseWikipediaTimestamp("2018-08-31T05:17:28Z"));
    }

    @Test
    void testFormatWikipediaDate() {
        String expected = "2018-08-31T05:17:28Z";
        assertEquals(
            expected,
            WikipediaDateUtils.formatWikipediaTimestamp(LocalDateTime.of(2018, Month.AUGUST, 31, 5, 17, 28))
        );
    }

    @Test
    void testReversibility() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime truncated = now.truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime reversed = WikipediaDateUtils.parseWikipediaTimestamp(
            WikipediaDateUtils.formatWikipediaTimestamp(now)
        );
        assertNotEquals(now, reversed);
        assertEquals(truncated, reversed);
    }
}
