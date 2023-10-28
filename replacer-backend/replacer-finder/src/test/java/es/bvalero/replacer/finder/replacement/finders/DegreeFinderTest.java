package es.bvalero.replacer.finder.replacement.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.finder.Replacement;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class DegreeFinderTest {

    private DegreeFinder degreeFinder;

    @BeforeEach
    public void setUp() {
        degreeFinder = new DegreeFinder();
    }

    @ParameterizedTest
    @CsvSource(
        delimiter = '|',
        value = {
            "50°C|50&nbsp;°C", // No space
            "50°F|50&nbsp;°F", // No space
            "50ºC|50&nbsp;°C", // Ordinal + No space
            "50 ºC|50&nbsp;°C", // Ordinal
            "50° C|50&nbsp;°C", // Inner space
            "50 ° C|50&nbsp;°C", // Inner space
            "50º C|50&nbsp;°C", // Ordinal + Inner space
            "50 º C|50&nbsp;°C", // Ordinal + Inner space
            "50&nbsp;° C|50&nbsp;°C", // Inner space
            "50&nbsp;ºC|50&nbsp;°C", // Ordinal
            "50&nbsp;º C|50&nbsp;°C", // Ordinal + Inner space
            "50 °K|50&nbsp;K", // Kelvin
            "50°K|50&nbsp;K", // Kelvin + No space
            "50 ºK|50&nbsp;K", // Kelvin + Ordinal
            "50ºK|50&nbsp;K", // Kelvin + Ordinal + No space
            "50 ℃|50&nbsp;°C", // Unicode
            "50.2°C|50.2&nbsp;°C", // Decimal + No space
            "50,2°C|50,2&nbsp;°C", // Decimal + No space
            "50{{esd}}ºC|50{{esd}}°C", // Ordinal
            "8.8{{esd}}℃|8.8{{esd}}°C", // Unicode
            "8.8&nbsp;℃|8.8&nbsp;°C", // Unicode
        }
    )
    void testDegree(String text, String expected) {
        List<Replacement> replacements = degreeFinder.findList(text);
        assertEquals(1, replacements.size());

        Replacement rep = replacements.get(0);
        assertEquals(StandardType.DEGREES, rep.getType());
        assertEquals(text, rep.getText());
        assertEquals(text, rep.getSuggestions().get(0).getText());
        assertEquals(expected, rep.getSuggestions().get(1).getText());
    }

    @ParameterizedTest
    @CsvSource(
        delimiter = '|',
        value = {
            "En ° C|° C|°C|3",
            "En ºC|ºC|°C|3", // With ordinal
            "En ºK|ºK|K|3",
            "En º C|º C|°C|3", // With ordinal
            "Punto de fusión ºC|ºC|°C|16",
            "Temperatura media (ºC)|ºC|°C|19",
        }
    )
    void testDegreeWithWordBefore(String text, String expected, String fix, int position) {
        List<Replacement> replacements = degreeFinder.findList(text);
        assertEquals(1, replacements.size());

        Replacement rep = replacements.get(0);
        assertEquals(StandardType.DEGREES, rep.getType());
        assertEquals(expected, rep.getText());
        assertEquals(position, rep.getStart());
        assertEquals(expected, rep.getSuggestions().get(0).getText());
        assertEquals(fix, rep.getSuggestions().get(1).getText());
    }

    @ParameterizedTest
    @ValueSource(strings = { "50 °C", "En °C", "50&nbsp;°C", "50.5{{esd}}°C", "50 K", "{{unidad|−273.144|°C}})" })
    void testValidDegree(String degree) {
        List<Replacement> replacements = degreeFinder.findList(degree);

        assertTrue(replacements.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "50 °L", // Not degree letter
            "ºC", // No word before
            "50 º", // No degree letter
            "50 º ", // No degree letter
        }
    )
    void testFalseDegree(String degree) {
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
