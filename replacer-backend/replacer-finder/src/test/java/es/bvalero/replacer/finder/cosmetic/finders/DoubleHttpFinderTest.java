package es.bvalero.replacer.finder.cosmetic.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.finder.Cosmetic;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class DoubleHttpFinderTest {

    private DoubleHttpFinder doubleHttpFinder;

    @BeforeEach
    public void setUp() {
        doubleHttpFinder = new DoubleHttpFinder();
    }

    @ParameterizedTest
    @CsvSource(
        value = {
            "http://http://marca.com, http://marca.com", "https://https://www.linkedin.com, https://www.linkedin.com",
        }
    )
    void testDoubleHttp(String text, String fix) {
        List<Cosmetic> cosmetics = doubleHttpFinder.findList(text);

        assertEquals(1, cosmetics.size());
        assertEquals(text, cosmetics.get(0).text());
        assertEquals(fix, cosmetics.get(0).fix());
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
