package es.bvalero.replacer.finder.cosmetic.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.finder.cosmetic.Cosmetic;
import es.bvalero.replacer.finder.cosmetic.checkwikipedia.CheckWikipediaService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class UnicodeWhiteSpacesFinderTest {

    @Mock
    private CheckWikipediaService checkWikipediaService;

    @InjectMocks
    private UnicodeWhiteSpaceFinder unicodeWhiteSpaceFinder;

    @BeforeEach
    public void setUp() {
        unicodeWhiteSpaceFinder = new UnicodeWhiteSpaceFinder();
        MockitoAnnotations.initMocks(this);
    }

    @ParameterizedTest
    @CsvSource(value = { "\u2002, ' '" })
    void testUnicodeWhiteSpace(String text, String fix) {
        List<Cosmetic> cosmetics = unicodeWhiteSpaceFinder.findList(text);

        assertEquals(1, cosmetics.size());
        assertEquals(text, cosmetics.get(0).getText());
        assertEquals(fix, cosmetics.get(0).getFix());
    }

    @ParameterizedTest
    @ValueSource(strings = { " " })
    void testNormalWhiteSpace(String text) {
        List<Cosmetic> cosmetics = unicodeWhiteSpaceFinder.findList(text);

        assertTrue(cosmetics.isEmpty());
    }
}
