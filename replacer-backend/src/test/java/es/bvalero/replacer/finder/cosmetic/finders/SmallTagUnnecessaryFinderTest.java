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

class SmallTagUnnecessaryFinderTest {

    @Mock
    private CheckWikipediaService checkWikipediaService;

    @InjectMocks
    private SmallTagUnnecessaryFinder smallTagUnnecessaryFinder;

    @BeforeEach
    public void setUp() {
        smallTagUnnecessaryFinder = new SmallTagUnnecessaryFinder();
        MockitoAnnotations.openMocks(this);
    }

    @ParameterizedTest
    @CsvSource(
        value = {
            "<sup><small>2</small></sup>, <sup>2</sup>",
            "<small><ref name=\"x\">Text</ref></small>, <ref name=\"x\">Text</ref>",
            "<ref name=\"x\"><small>Text</small></ref>, <ref name=\"x\">Text</ref>",
        }
    )
    void testSmallTagUnnecessaryFinder(String text, String fix) {
        List<Cosmetic> cosmetics = smallTagUnnecessaryFinder.findList(text);

        assertEquals(1, cosmetics.size());
        assertEquals(text, cosmetics.get(0).getText());
        assertEquals(fix, cosmetics.get(0).getFix());
    }

    @ParameterizedTest
    @ValueSource(strings = { "<small>Text</small>" })
    void testSingleSmallTag(String text) {
        List<Cosmetic> cosmetics = smallTagUnnecessaryFinder.findList(text);

        assertTrue(cosmetics.isEmpty());
    }
}
