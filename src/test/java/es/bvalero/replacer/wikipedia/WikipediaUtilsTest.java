package es.bvalero.replacer.wikipedia;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeParseException;

public class WikipediaUtilsTest {

    @Test
    public void testParseWikipediaDate() {
        LocalDateTime expected = LocalDateTime.of(2018, Month.AUGUST, 31, 5, 17, 28);
        Assert.assertEquals(expected, WikipediaUtils.parseWikipediaDate("2018-08-31T05:17:28Z"));
    }

    @Test(expected = DateTimeParseException.class)
    public void testParseWikipediaDateBadFormat() {
        WikipediaUtils.parseWikipediaDate("xxx");
    }

    @Test
    public void testIsRedirectionArticle() {
        Assert.assertTrue(WikipediaUtils.isRedirectionArticle("xxx #REDIRECCIÓN [[A]] yyy"));
        Assert.assertTrue(WikipediaUtils.isRedirectionArticle("xxx #redirección [[A]] yyy"));
        Assert.assertTrue(WikipediaUtils.isRedirectionArticle("xxx #REDIRECT [[A]] yyy"));
        Assert.assertFalse(WikipediaUtils.isRedirectionArticle("Otro contenido"));
    }

}
