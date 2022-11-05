package es.bvalero.replacer.finder.replacement.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.common.domain.Replacement;
import es.bvalero.replacer.common.domain.ReplacementType;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class DegreeFinderTest {

    private final DegreeFinder degreeFinder = new DegreeFinder();

    @ParameterizedTest
    @CsvSource(
        {
            "50°C, 50&nbsp;°C",
            "50°F, 50&nbsp;°F",
            "50ºC, 50&nbsp;°C", // With ordinal
            "50 ºC, 50&nbsp;°C", // With ordinal
            "50° C, 50&nbsp;°C",
            "50 ° C, 50&nbsp;°C",
            "50º C, 50&nbsp;°C", // With ordinal
            "50 º C, 50&nbsp;°C", // With ordinal
            "En ° C, En °C",
            "En ºC, En °C", // With ordinal
            "En º C, En °C", // With ordinal
            "50&nbsp;° C, 50&nbsp;°C",
            "50&nbsp;ºC, 50&nbsp;°C", // With ordinal
            "50&nbsp;º C, 50&nbsp;°C", // With ordinal
            "50 °K, 50&nbsp;K",
            "50°K, 50&nbsp;K",
            "50 ºK, 50&nbsp;K", // With ordinal
            "50ºK, 50&nbsp;K", // With ordinal
            "50 °c, 50&nbsp;°C",
        }
    )
    void testDegree(String text, String expected) {
        List<Replacement> replacements = degreeFinder.findList(text);
        assertEquals(1, replacements.size());

        Replacement rep = replacements.get(0);
        assertEquals(ReplacementType.DEGREES, rep.getType());
        assertEquals(text, rep.getText());
        assertEquals(text, rep.getSuggestions().get(0).getText());
        assertEquals(expected, rep.getSuggestions().get(1).getText());
    }

    @ParameterizedTest
    @ValueSource(strings = { "50 °C", "En °C", "50&nbsp;°C", "50 K" })
    void testValidDegree(String degree) {
        List<Replacement> replacements = degreeFinder.findList(degree);

        assertTrue(replacements.isEmpty());
    }
}
