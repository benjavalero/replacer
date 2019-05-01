package es.bvalero.replacer.wikipedia;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RedirectMatcherTest {

    private String text;

    @Before
    public void setUp() {
        this.text = "#Redirección [[Julio]]";
    }

    @Test
    public void testMatchRedirectContainsLower() {
        RedirectContainsLowerMatcher matcher = new RedirectContainsLowerMatcher();
        Assert.assertTrue(matcher.isRedirect(text));
    }

    @Test
    public void testMatchRedirectContainsIgnore() {
        RedirectContainsIgnoreMatcher matcher = new RedirectContainsIgnoreMatcher();
        Assert.assertTrue(matcher.isRedirect(text));
    }

    @Test
    public void testMatchRedirectRegexInsensitive() {
        RedirectRegexInsensitiveMatcher matcher = new RedirectRegexInsensitiveMatcher();
        Assert.assertTrue(matcher.isRedirect(text));
    }

}
