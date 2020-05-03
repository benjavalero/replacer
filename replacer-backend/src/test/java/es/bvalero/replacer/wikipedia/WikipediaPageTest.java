package es.bvalero.replacer.wikipedia;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;

public class WikipediaPageTest {

    @Test
    public void testIsProcessableByNamespace() {
        Assertions.assertFalse(WikipediaPage.builder().namespace(WikipediaNamespace.WIKIPEDIA).build().isProcessableByNamespace());
        Assertions.assertTrue(WikipediaPage.builder().namespace(WikipediaNamespace.ARTICLE).build().isProcessableByNamespace());
        Assertions.assertTrue(WikipediaPage.builder().namespace(WikipediaNamespace.ANNEX).build().isProcessableByNamespace());
    }

    @Test
    public void testIsProcessableByContent() {
        Assertions.assertFalse(WikipediaPage.builder().content("xxx #REDIRECCIÓN [[A]] yyy").build().isProcessableByContent());
        Assertions.assertFalse(WikipediaPage.builder().content("xxx #redirección [[A]] yyy").build().isProcessableByContent());
        Assertions.assertFalse(WikipediaPage.builder().content("xxx #REDIRECT [[A]] yyy").build().isProcessableByContent());
        Assertions.assertTrue(WikipediaPage.builder().content("Otro contenido").build().isProcessableByContent());
        Assertions.assertFalse(WikipediaPage.builder().content("xxx {{destruir|motivo}}").build().isProcessableByContent());
    }

    @Test
    public void testParseWikipediaDate() {
        LocalDate expected = LocalDate.of(2018, Month.AUGUST, 31);
        Assertions.assertEquals(expected, WikipediaPage.parseWikipediaTimestamp("2018-08-31T05:17:28Z"));
    }

    @Test
    public void testFormatWikipediaDate() {
        String expected = "2018-08-31T05:17:28Z";
        Assertions.assertEquals(expected, WikipediaPage.formatWikipediaTimestamp(
                LocalDateTime.of(2018, Month.AUGUST, 31, 5, 17, 28)));
    }

}
