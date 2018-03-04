package es.bvalero.replacer.wikipedia;

import org.junit.Assert;
import org.junit.Test;

public class WikipediaUtilsTest {

    @Test
    public void testParseWikipediaDateBadFormat() {
        Assert.assertNull(WikipediaUtils.parseWikipediaDate("xxx"));
    }

    @Test
    public void testIsRedirectionArticle() {
        Assert.assertTrue(WikipediaUtils.isRedirectionArticle("xxx #REDIRECCIÓN [[A]] yyy"));
        Assert.assertTrue(WikipediaUtils.isRedirectionArticle("xxx #redirección [[A]] yyy"));
        Assert.assertTrue(WikipediaUtils.isRedirectionArticle("xxx #REDIRECT [[A]] yyy"));
        Assert.assertFalse(WikipediaUtils.isRedirectionArticle("Otro contenido"));
    }

}
