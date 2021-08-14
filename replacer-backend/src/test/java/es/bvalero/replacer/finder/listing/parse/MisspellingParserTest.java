package es.bvalero.replacer.finder.listing.parse;

import es.bvalero.replacer.finder.listing.SimpleMisspelling;
import java.util.Collection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MisspellingParserTest {

    private SimpleMisspellingParser simpleMisspellingParser;

    @BeforeEach
    public void setUp() {
        // We could do the test with simple or composed parser
        simpleMisspellingParser = new SimpleMisspellingParser();
    }

    @Test
    void testParseMisspellingListText() {
        String misspellingListText =
            "Text\n\n" +
            "A||B\n" + // No starting whitespace
            " C|cs|D\n" +
            " E|CS|F\n" +
            " G|H\n" + // Bad formatted
            " I||J\n" +
            " I||J\n" + // Duplicated
            " k||k\n" +
            " k||M\n"; // Duplicated but different comment

        Collection<SimpleMisspelling> misspellings = simpleMisspellingParser.parseListing(misspellingListText);
        Assertions.assertEquals(4, misspellings.size());
        Assertions.assertTrue(misspellings.contains(SimpleMisspelling.of("C", true, "D")));
        Assertions.assertTrue(misspellings.contains(SimpleMisspelling.of("E", true, "F")));
        Assertions.assertTrue(misspellings.contains(SimpleMisspelling.of("I", false, "J")));
        Assertions.assertTrue(misspellings.contains(SimpleMisspelling.of("k", false, "k")));
    }
}
