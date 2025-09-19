package es.bvalero.replacer.finder.cosmetic.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.bvalero.replacer.finder.Cosmetic;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ListBreakFinderTest {

    private ListBreakFinder listBreakFinder;

    @BeforeEach
    public void setUp() {
        listBreakFinder = new ListBreakFinder();
    }

    @ParameterizedTest
    @CsvSource(value = { "* x <br>, * x", "* z<br />, * z" })
    void testListBreak(String text, String fix) {
        List<Cosmetic> cosmetics = listBreakFinder.findList(text);

        assertEquals(1, cosmetics.size());
        assertEquals(text, cosmetics.get(0).text());
        assertEquals(fix, cosmetics.get(0).fix());
    }

    @Test
    void testListBreakWithMoreValues() {
        String text = "* x\n* y<br>\n* z <br />\nText";

        List<Cosmetic> cosmetics = listBreakFinder.findList(text);

        assertEquals(2, cosmetics.size());
        assertEquals("* y<br>", cosmetics.get(0).text());
        assertEquals("* y", cosmetics.get(0).fix());
        assertEquals("* z <br />", cosmetics.get(1).text());
        assertEquals("* z", cosmetics.get(1).fix());
    }
}
