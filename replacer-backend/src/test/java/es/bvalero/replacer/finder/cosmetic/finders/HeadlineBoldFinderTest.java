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

class HeadlineBoldFinderTest {

    @Mock
    private CheckWikipediaService checkWikipediaService;

    @InjectMocks
    private HeadlineBoldFinder headlineBoldFinder;

    @BeforeEach
    public void setUp() {
        headlineBoldFinder = new HeadlineBoldFinder();
        MockitoAnnotations.openMocks(this);
    }

    @ParameterizedTest
    @CsvSource(value = { "== '''Asia''' ==, == Asia ==", "==='''Asia'''===, === Asia ===" })
    void testHeadlineBold(String text, String fix) {
        List<Cosmetic> cosmetics = headlineBoldFinder.findList(text);

        assertEquals(1, cosmetics.size());
        assertEquals(text, cosmetics.get(0).getText());
        assertEquals(fix, cosmetics.get(0).getFix());
    }

    @ParameterizedTest
    @ValueSource(strings = { "=== Asia ===", "=== '''Asia ===", "== ''Asia'' ==" })
    void testNormalHeadline(String text) {
        List<Cosmetic> cosmetics = headlineBoldFinder.findList(text);

        assertTrue(cosmetics.isEmpty());
    }
}
