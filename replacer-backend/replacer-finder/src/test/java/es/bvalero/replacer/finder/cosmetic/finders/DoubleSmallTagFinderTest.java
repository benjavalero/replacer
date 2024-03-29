package es.bvalero.replacer.finder.cosmetic.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.finder.Cosmetic;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class DoubleSmallTagFinderTest {

    private DoubleSmallTagFinder doubleSmallTagFinder;

    @BeforeEach
    public void setUp() {
        doubleSmallTagFinder = new DoubleSmallTagFinder();
    }

    @ParameterizedTest
    @CsvSource(value = { "<small><small>Text</small></small>, <small>Text</small>" })
    void testDoubleSmallTag(String text, String fix) {
        List<Cosmetic> cosmetics = doubleSmallTagFinder.findList(text);

        assertEquals(1, cosmetics.size());
        assertEquals(text, cosmetics.get(0).getText());
        assertEquals(fix, cosmetics.get(0).getFix());
    }

    @ParameterizedTest
    @ValueSource(strings = { "<small>Text</small>" })
    void testSingleSmallTag(String text) {
        List<Cosmetic> cosmetics = doubleSmallTagFinder.findList(text);

        assertTrue(cosmetics.isEmpty());
    }
}
