package es.bvalero.replacer.wikipedia;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
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
        LocalDate expected = LocalDate.of(2018, Month.AUGUST, 31);
        WikipediaPage page = WikipediaPage.builder().setTimestamp("2018-08-31T05:17:28Z").build();
        Assert.assertEquals(expected, page.getTimestamp());
    }

    @Test(expected = DateTimeParseException.class)
    public void testParseWikipediaDateBadFormat() {
        WikipediaPage.builder().setTimestamp("xxx").build();
    }

}
