package es.bvalero.replacer.wikipedia;

import org.junit.Assert;
import org.junit.Test;

public class WikipediaServiceTest {

    @Test
    public void testIsRedirectionPage() {
        Assert.assertTrue(WikipediaPage.builder().setContent("xxx #REDIRECCIÓN [[A]] yyy").build().isRedirectionPage());
        Assert.assertTrue(WikipediaPage.builder().setContent("xxx #redirección [[A]] yyy").build().isRedirectionPage());
        Assert.assertTrue(WikipediaPage.builder().setContent("xxx #REDIRECT [[A]] yyy").build().isRedirectionPage());
        Assert.assertFalse(WikipediaPage.builder().setContent("Otro contenido").build().isRedirectionPage());
    }

}
