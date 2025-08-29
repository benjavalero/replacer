package es.bvalero.replacer.wikipedia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;

class WikipediaTimestampTest {

    @Test
    void testParseWikipediaDate() {
        LocalDateTime expected = LocalDateTime.of(2018, Month.AUGUST, 31, 5, 17, 28);
        assertEquals(expected, WikipediaTimestamp.of("2018-08-31T05:17:28Z").toLocalDateTime());
    }

    @Test
    void testFormatWikipediaDate() {
        String expected = "2018-08-31T05:17:28Z";
        assertEquals(expected, WikipediaTimestamp.of(LocalDateTime.of(2018, Month.AUGUST, 31, 5, 17, 28)).toString());
    }

    @Test
    void testReversibility() {
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        LocalDateTime truncated = now.truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime reversed = WikipediaTimestamp.of(WikipediaTimestamp.of(now).toString()).toLocalDateTime();
        assertNotEquals(now, reversed);
        assertEquals(truncated, reversed);
    }
}
