package es.bvalero.replacer.finder.benchmark.redirect;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RedirectMatcherTest {

    private String text;

    @BeforeEach
    public void setUp() {
        this.text = "#Redirección [[Julio]]";
    }

    @Test
    void testRedirectLowercaseContainsMatcher() {
        RedirectLowercaseContainsMatcher matcher = new RedirectLowercaseContainsMatcher();
        Assertions.assertTrue(matcher.isRedirect(text));
    }

    @Test
    void testRedirectContainsIgnoreCaseMatcher() {
        RedirectContainsIgnoreCaseMatcher matcher = new RedirectContainsIgnoreCaseMatcher();
        Assertions.assertTrue(matcher.isRedirect(text));
    }

    @Test
    void testRedirectRegexInsensitiveMatcher() {
        RedirectRegexInsensitiveMatcher matcher = new RedirectRegexInsensitiveMatcher();
        Assertions.assertTrue(matcher.isRedirect(text));
    }
}
