package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.finder.Immutable;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class InterLanguageLinkFinderTest {

    private final InterLanguageLinkFinder interLanguageLinkFinder = new InterLanguageLinkFinder();

    @ParameterizedTest
    @ValueSource(
        strings = { "[[sv:Renkavle]]", "[[fr:Compression de données#Compression avec pertes]]", "[[zh:浮游生物]]" }
    )
    void testRegexInterLanguageLink(String text) {
        List<Immutable> matches = interLanguageLinkFinder.findList(text);

        Assertions.assertEquals(1, matches.size());
        Assertions.assertEquals(text, matches.get(0).getText());
    }

    @ParameterizedTest
    @ValueSource(strings = { "[[s:es:Corán|Corán]]", "[[:en:Constitution of Virginia]]", "[[dc:Tierra Uno|Tierra- 1]]" })
    void testInterLanguageNonValid(String text) {
        List<Immutable> matches = interLanguageLinkFinder.findList(text);

        Assertions.assertTrue(matches.isEmpty());
    }
}
