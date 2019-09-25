package es.bvalero.replacer.wikipedia;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.Month;

public class WikipediaPageTest {

    @Test
    public void testIsProcessableByContent() {
        Assert.assertFalse(WikipediaPage.builder().content("xxx #REDIRECCIÓN [[A]] yyy").build().isProcessableByContent());
        Assert.assertFalse(WikipediaPage.builder().content("xxx #redirección [[A]] yyy").build().isProcessableByContent());
        Assert.assertFalse(WikipediaPage.builder().content("xxx #REDIRECT [[A]] yyy").build().isProcessableByContent());
        Assert.assertTrue(WikipediaPage.builder().content("Otro contenido").build().isProcessableByContent());
        Assert.assertFalse(WikipediaPage.builder().content("xxx {{destruir|motivo}}").build().isProcessableByContent());
    }

    @Test
    public void testParseWikipediaDate() {
        LocalDateTime expected = LocalDateTime.of(2018, Month.AUGUST, 31, 5, 17, 28);
        Assert.assertEquals(expected, WikipediaPage.parseWikipediaTimestamp("2018-08-31T05:17:28Z"));
    }

}
