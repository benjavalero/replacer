package es.bvalero.replacer.finder.replacement.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.common.domain.Replacement;
import es.bvalero.replacer.common.domain.ReplacementType;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class DegreeFinderTest {

    private final DegreeFinder degreeFinder = new DegreeFinder();

    @ParameterizedTest
    @CsvSource(
        delimiter = '|',
        value = {
            "50°C| 50&nbsp;°C",
            "50°F| 50&nbsp;°F",
            "50ºC| 50&nbsp;°C", // With ordinal
            "50 ºC| 50&nbsp;°C", // With ordinal
            "50° C| 50&nbsp;°C",
            "50 ° C| 50&nbsp;°C",
            "50º C| 50&nbsp;°C", // With ordinal
            "50 º C| 50&nbsp;°C", // With ordinal
            "En ° C| En °C",
            "En ºC| En °C", // With ordinal
            "En º C| En °C", // With ordinal
            "50&nbsp;° C| 50&nbsp;°C",
            "50&nbsp;ºC| 50&nbsp;°C", // With ordinal
            "50&nbsp;º C| 50&nbsp;°C", // With ordinal
            "50 °K| 50&nbsp;K",
            "50°K| 50&nbsp;K",
            "50 ºK| 50&nbsp;K", // With ordinal
            "50ºK| 50&nbsp;K", // With ordinal
            "50 °c| 50&nbsp;°C",
            "50 ℃| 50&nbsp;°C",
            "50.2°C| 50.2&nbsp;°C",
            "50,2°C| 50,2&nbsp;°C",
            "50{{esd}}ºC| 50{{esd}}°C",
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
    @ValueSource(strings = { "50 °C", "En °C", "50&nbsp;°C", "50{{esd}}°C", "50 K" })
    void testValidDegree(String degree) {
        List<Replacement> replacements = degreeFinder.findList(degree);

        assertTrue(replacements.isEmpty());
    }

    @Test
    void testSeveralDegrees() {
        // The 3 cases use the masculine ordinal
        String text =
            "|PEK=417.15|(o-xileno:144&nbsp;ºC); 412.15|(m-xileno:139&nbsp;ºC); 411.15|(p-xileno:138&nbsp;ºC)";

        List<Replacement> replacements = degreeFinder.findList(text);

        assertEquals(3, replacements.size());
    }
}
