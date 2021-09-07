package es.bvalero.replacer.finder.benchmark.ignorabletemplate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IgnorableTemplateFinderTest {

    private String text;

    @BeforeEach
    public void setUp() {
        this.text = "#Redirección [[Julio]]";
    }

    @Test
    void testRedirectLowercaseContainsMatcher() {
        IgnorableTemplateLowercaseContainsFinder matcher = new IgnorableTemplateLowercaseContainsFinder();
        Assertions.assertTrue(matcher.isRedirect(text));
    }

    @Test
    void testRedirectContainsIgnoreCaseMatcher() {
        IgnorableTemplateContainsIgnoreCaseFinder matcher = new IgnorableTemplateContainsIgnoreCaseFinder();
        Assertions.assertTrue(matcher.isRedirect(text));
    }

    @Test
    void testRedirectRegexInsensitiveMatcher() {
        IgnorableTemplateRegexInsensitiveFinder matcher = new IgnorableTemplateRegexInsensitiveFinder();
        Assertions.assertTrue(matcher.isRedirect(text));
    }
}
