package es.bvalero.replacer.finder.cosmetic.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.finder.Cosmetic;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class HeadlineBoldFinderTest {

    private HeadlineBoldFinder headlineBoldFinder;

    @BeforeEach
    public void setUp() {
        headlineBoldFinder = new HeadlineBoldFinder();
    }

    @ParameterizedTest
    @CsvSource(value = { "== '''Asia''' ==, == Asia ==", "==='''Asia'''===, === Asia ===" })
    void testHeadlineBold(String text, String fix) {
        List<Cosmetic> cosmetics = headlineBoldFinder.findList(text);

        assertEquals(1, cosmetics.size());
        assertEquals(text, cosmetics.get(0).text());
        assertEquals(fix, cosmetics.get(0).fix());
    }

    @ParameterizedTest
    @ValueSource(strings = { "=== Asia ===", "=== '''Asia ===", "== ''Asia'' ==" })
    void testNormalHeadline(String text) {
        List<Cosmetic> cosmetics = headlineBoldFinder.findList(text);

        assertTrue(cosmetics.isEmpty());
    }
}
