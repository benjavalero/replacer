package es.bvalero.replacer.wikipedia;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeParseException;

public class WikipediaPageTest {

    @Test
    public void testIsRedirectionPage() {
        Assert.assertTrue(WikipediaPage.builder().setContent("xxx #REDIRECCIÓN [[A]] yyy").build().isRedirectionPage());
        Assert.assertTrue(WikipediaPage.builder().setContent("xxx #redirección [[A]] yyy").build().isRedirectionPage());
        Assert.assertTrue(WikipediaPage.builder().setContent("xxx #REDIRECT [[A]] yyy").build().isRedirectionPage());
        Assert.assertFalse(WikipediaPage.builder().setContent("Otro contenido").build().isRedirectionPage());
    }

    @Test
    public void testParseWikipediaDate() {
        LocalDateTime expected = LocalDateTime.of(2018, Month.AUGUST, 31, 5, 17, 28);
        Assert.assertEquals(expected, WikipediaPage.parseWikipediaTimestamp("2018-08-31T05:17:28Z"));
    }

    @Test(expected = DateTimeParseException.class)
    public void testParseWikipediaDateBadFormat() {
        WikipediaPage page = WikipediaPage.builder().setTimestamp("xxx").build();
        page.getLastUpdate();
    }

}
