package es.bvalero.replacer.finder.benchmark;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

// TODO: Adapt to Junit5
public class RedirectMatcherTest {
    private String text;

    @Before
    public void setUp() {
        this.text = "#Redirección [[Julio]]";
    }

    @Test
    void testRedirectLowercaseContainsMatcher() {
        RedirectLowercaseContainsMatcher matcher = new RedirectLowercaseContainsMatcher();
        Assert.assertTrue(matcher.isRedirect(text));
    }

    @Test
    void testRedirectContainsIgnoreCaseMatcher() {
        RedirectContainsIgnoreCaseMatcher matcher = new RedirectContainsIgnoreCaseMatcher();
        Assert.assertTrue(matcher.isRedirect(text));
    }

    @Test
    void testRedirectRegexInsensitiveMatcher() {
        RedirectRegexInsensitiveMatcher matcher = new RedirectRegexInsensitiveMatcher();
        Assert.assertTrue(matcher.isRedirect(text));
    }
}
