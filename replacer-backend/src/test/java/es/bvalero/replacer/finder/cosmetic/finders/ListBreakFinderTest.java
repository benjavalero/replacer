package es.bvalero.replacer.finder.cosmetic.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.bvalero.replacer.common.domain.Cosmetic;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ListBreakFinderTest {



    @InjectMocks
    private ListBreakFinder listBreakFinder;

    @BeforeEach
    public void setUp() {
        listBreakFinder = new ListBreakFinder();
        MockitoAnnotations.openMocks(this);
    }

    @ParameterizedTest
    @CsvSource(value = { "* x <br>, * x", "* z<br />, * z" })
    void testListBreakFinder(String text, String fix) {
        List<Cosmetic> cosmetics = listBreakFinder.findList(text);

        assertEquals(1, cosmetics.size());
        assertEquals(text, cosmetics.get(0).getText());
        assertEquals(fix, cosmetics.get(0).getFix());
    }

    @Test
    void testListBreakWithMoreValues() {
        String text = "* x\n* y<br>\n* z <br />\nText";

        List<Cosmetic> cosmetics = listBreakFinder.findList(text);

        assertEquals(2, cosmetics.size());
        assertEquals("* y<br>", cosmetics.get(0).getText());
        assertEquals("* y", cosmetics.get(0).getFix());
        assertEquals("* z <br />", cosmetics.get(1).getText());
        assertEquals("* z", cosmetics.get(1).getFix());
    }
}
