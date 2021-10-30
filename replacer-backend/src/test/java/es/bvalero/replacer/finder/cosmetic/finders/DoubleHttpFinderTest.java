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

class DoubleHttpFinderTest {

    @Mock
    private CheckWikipediaService checkWikipediaService;

    @InjectMocks
    private DoubleHttpFinder doubleHttpFinder;

    @BeforeEach
    public void setUp() {
        doubleHttpFinder = new DoubleHttpFinder();
        MockitoAnnotations.initMocks(this);
    }

    @ParameterizedTest
    @CsvSource(
        value = {
            "http://http://marca.com, http://marca.com", "https://https://www.linkedin.com, https://www.linkedin.com",
        }
    )
    void testDoubleHttpFinder(String text, String fix) {
        List<Cosmetic> cosmetics = doubleHttpFinder.findList(text);

        assertEquals(1, cosmetics.size());
        assertEquals(text, cosmetics.get(0).getText());
        assertEquals(fix, cosmetics.get(0).getFix());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "http://marca.com",
            "https://www.linkedin.com",
            "http://https://www.linkedin.com",
            "https://http://marca.com",
        }
    )
    void testValidExternalLink(String text) {
        List<Cosmetic> cosmetics = doubleHttpFinder.findList(text);

        assertTrue(cosmetics.isEmpty());
    }
}
