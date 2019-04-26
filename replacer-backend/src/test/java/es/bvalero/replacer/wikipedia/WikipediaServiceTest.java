package es.bvalero.replacer.wikipedia;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class WikipediaServiceTest {

    private WikipediaService wikipediaService;

    @Before
    public void setUp() {
        wikipediaService = new WikipediaService();
    }

    @Test
    public void testIsRedirectionPage() {
        Assert.assertTrue(wikipediaService.isRedirectionPage("xxx #REDIRECCIÓN [[A]] yyy"));
        Assert.assertTrue(wikipediaService.isRedirectionPage("xxx #redirección [[A]] yyy"));
        Assert.assertTrue(wikipediaService.isRedirectionPage("xxx #REDIRECT [[A]] yyy"));
        Assert.assertFalse(wikipediaService.isRedirectionPage("Otro contenido"));
    }

}
