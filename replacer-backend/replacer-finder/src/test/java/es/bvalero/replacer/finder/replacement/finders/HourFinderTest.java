package es.bvalero.replacer.finder.replacement.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.finder.Replacement;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class HourFinderTest {

    private HourFinder hourFinder;

    @BeforeEach
    public void setUp() {
        hourFinder = new HourFinder();
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = { "38:05'08''|38:05′08″", "38:05'08.325''|38:05′08.325″" })
    void testHours(String text, String expected) {
        List<Replacement> replacements = hourFinder.findList(text);
        assertEquals(1, replacements.size());

        Replacement rep = replacements.get(0);
        assertEquals(StandardType.HOURS, rep.getType());
        assertEquals(text, rep.getText());
        assertEquals(text, rep.getSuggestions().get(0).getText());
        assertEquals(expected, rep.getSuggestions().get(1).getText());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "38:05′08″", "38:05′", // Hours without seconds are ignored
        }
    )
    void testValidHours(String text) {
        List<Replacement> replacements = hourFinder.findList(text);

        assertTrue(replacements.isEmpty());
    }
}
