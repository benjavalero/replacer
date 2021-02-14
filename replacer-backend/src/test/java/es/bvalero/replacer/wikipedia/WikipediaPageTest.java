package es.bvalero.replacer.wikipedia;

import es.bvalero.replacer.common.DateUtils;
import es.bvalero.replacer.config.XmlConfiguration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = XmlConfiguration.class)
class WikipediaPageTest {

    @Test
    void testParseWikipediaDate() {
        LocalDate expected = LocalDate.of(2018, Month.AUGUST, 31);
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
