package es.bvalero.replacer.finder.cosmetic.finders;

import static es.bvalero.replacer.finder.cosmetic.finders.BreakIncorrectFinder.BREAK_XHTML;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.finder.Cosmetic;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BreakIncorrectFinderTest {

    private BreakIncorrectFinder breakIncorrectFinder;

    @BeforeEach
    public void setUp() {
        breakIncorrectFinder = new BreakIncorrectFinder();
    }

    @ParameterizedTest
    @ValueSource(strings = { "</br>", "<\\br>", "<br.>", "<br \\>", "<br >" })
    void testBreakIncorrect(String text) {
        List<Cosmetic> cosmetics = breakIncorrectFinder.findList(text);

        assertEquals(1, cosmetics.size());
        assertEquals(text, cosmetics.get(0).getText());
        assertEquals(BREAK_XHTML, cosmetics.get(0).getFix());
    }

    @ParameterizedTest
    @ValueSource(strings = { "<br>", "<br />", "<br/>" })
    void testValidBreak(String text) {
        List<Cosmetic> cosmetics = breakIncorrectFinder.findList(text);

        assertTrue(cosmetics.isEmpty());
    }
}
