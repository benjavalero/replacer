package es.bvalero.replacer.finder.cosmetic.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.bvalero.replacer.finder.cosmetic.Cosmetic;
import es.bvalero.replacer.finder.cosmetic.checkwikipedia.CheckWikipediaService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
        MockitoAnnotations.initMocks(this);
    }

    @ParameterizedTest
    @CsvSource(value = { "</br>, <br />" })
    void testBreakIncorrectFinder(String text, String fix) {
        List<Cosmetic> cosmetics = breakIncorrectFinder.findList(text);

        assertEquals(1, cosmetics.size());
        assertEquals(text, cosmetics.get(0).getText());
        assertEquals(fix, cosmetics.get(0).getFix());
    }
}
