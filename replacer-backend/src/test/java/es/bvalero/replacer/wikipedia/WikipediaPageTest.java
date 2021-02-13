package es.bvalero.replacer.wikipedia;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaNamespace;
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
    void testIsProcessableByNamespace() throws ReplacerException {
        Assertions.assertThrows(
            ReplacerException.class,
            () ->
                WikipediaPage.builder().namespace(WikipediaNamespace.WIKIPEDIA).build().validateProcessableByNamespace()
        );
        WikipediaPage.builder().namespace(WikipediaNamespace.ARTICLE).build().validateProcessableByNamespace();
        WikipediaPage.builder().namespace(WikipediaNamespace.ANNEX).build().validateProcessableByNamespace();
    }

    @Test
    void testParseWikipediaDate() {
        LocalDate expected = LocalDate.of(2018, Month.AUGUST, 31);
        Assertions.assertEquals(expected, WikipediaUtils.parseWikipediaTimestamp("2018-08-31T05:17:28Z"));
    }

    @Test
    void testFormatWikipediaDate() {
        String expected = "2018-08-31T05:17:28Z";
        Assertions.assertEquals(
            expected,
            WikipediaUtils.formatWikipediaTimestamp(LocalDateTime.of(2018, Month.AUGUST, 31, 5, 17, 28))
        );
    }
}
