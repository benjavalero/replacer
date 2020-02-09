package es.bvalero.replacer.finder.benchmark;

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
    public void testRedirectLowercaseContainsMatcher() {
        RedirectLowercaseContainsMatcher matcher = new RedirectLowercaseContainsMatcher();
        Assert.assertTrue(matcher.isRedirect(text));
    }

    @Test
    public void testRedirectContainsIgnoreCaseMatcher() {
        RedirectContainsIgnoreCaseMatcher matcher = new RedirectContainsIgnoreCaseMatcher();
        Assert.assertTrue(matcher.isRedirect(text));
    }

    @Test
    public void testRedirectRegexInsensitiveMatcher() {
        RedirectRegexInsensitiveMatcher matcher = new RedirectRegexInsensitiveMatcher();
        Assert.assertTrue(matcher.isRedirect(text));
    }

}
