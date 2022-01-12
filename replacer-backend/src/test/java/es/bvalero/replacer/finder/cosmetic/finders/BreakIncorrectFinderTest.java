package es.bvalero.replacer.finder.cosmetic.finders;

import static es.bvalero.replacer.finder.cosmetic.finders.BreakIncorrectFinder.BREAK_HTML5;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.finder.cosmetic.Cosmetic;
import es.bvalero.replacer.finder.cosmetic.checkwikipedia.CheckWikipediaService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class BreakIncorrectFinderTest {

    @Mock
    private CheckWikipediaService checkWikipediaService;

    @InjectMocks
    private BreakIncorrectFinder breakIncorrectFinder;

    @BeforeEach
    public void setUp() {
        breakIncorrectFinder = new BreakIncorrectFinder();
        MockitoAnnotations.openMocks(this);
    }

    @ParameterizedTest
    @ValueSource(strings = { "</br>", "<\\br>", "<br.>", "<br \\>", "<br/>" })
    void testBreakIncorrectFinder(String text) {
        List<Cosmetic> cosmetics = breakIncorrectFinder.findList(text);

        assertEquals(1, cosmetics.size());
        assertEquals(text, cosmetics.get(0).getText());
        assertEquals(BREAK_HTML5, cosmetics.get(0).getFix());
    }

    @ParameterizedTest
    @ValueSource(strings = { "<br>", "<br />" })
    void testValidBreak(String text) {
        List<Cosmetic> cosmetics = breakIncorrectFinder.findList(text);

        assertTrue(cosmetics.isEmpty());
    }
}
