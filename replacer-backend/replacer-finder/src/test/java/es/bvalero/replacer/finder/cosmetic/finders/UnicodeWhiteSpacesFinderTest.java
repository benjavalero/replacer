package es.bvalero.replacer.finder.cosmetic.finders;

import static es.bvalero.replacer.finder.util.FinderUtils.SPACE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.finder.Cosmetic;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class UnicodeWhiteSpacesFinderTest {

    private UnicodeWhiteSpaceFinder unicodeWhiteSpaceFinder;

    @BeforeEach
    public void setUp() {
        unicodeWhiteSpaceFinder = new UnicodeWhiteSpaceFinder();
    }

    @ParameterizedTest
    @CsvSource(value = { "\u2002, ' '" })
    void testUnicodeWhiteSpace(String text, String fix) {
        List<Cosmetic> cosmetics = unicodeWhiteSpaceFinder.findList(text);

        assertEquals(1, cosmetics.size());
        assertEquals(text, cosmetics.get(0).text());
        assertEquals(fix, cosmetics.get(0).fix());
    }

    @ParameterizedTest
    @ValueSource(strings = { SPACE })
    void testNormalWhiteSpace(String text) {
        List<Cosmetic> cosmetics = unicodeWhiteSpaceFinder.findList(text);

        assertTrue(cosmetics.isEmpty());
    }
}
