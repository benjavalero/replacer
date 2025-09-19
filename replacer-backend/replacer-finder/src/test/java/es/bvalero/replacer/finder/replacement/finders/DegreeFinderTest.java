package es.bvalero.replacer.finder.replacement.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.StandardType;
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

        Replacement rep = replacements.getFirst();
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

        Replacement rep = replacements.getFirst();
        assertEquals(StandardType.DEGREES, rep.getType());
        assertEquals(expected, rep.getText());
        assertEquals(position, rep.getStart());
        assertEquals(expected, rep.getSuggestions().get(0).getText());
        assertEquals(fix, rep.getSuggestions().get(1).getText());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "50 °C", "En °C", "50&nbsp;°C", "50.5{{esd}}°C", "50 K", "{{unidad|−273.144|°C}})", "16,4&nbsp;[[°C]]",
        }
    )
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

    @Test
    void testDegreeAfterFalsePositive() {
        String text = "Coord: 18°55' Temp: 34ºC";

        List<Replacement> replacements = degreeFinder.findList(text);

        assertEquals(1, replacements.size());
        assertEquals("34ºC", replacements.get(0).getText());
    }

    @Test
    void testDegreePossiblyOrdinal() {
        String text = "En el grupo 1ºC.";

        List<Replacement> replacements = degreeFinder.findList(text);

        assertEquals(1, replacements.size());
        Replacement rep = replacements.getFirst();
        assertEquals(3, rep.getSuggestions().size());
        assertEquals("1ºC", rep.getSuggestions().get(0).getText());
        assertEquals("1&nbsp;°C", rep.getSuggestions().get(1).getText());
        assertEquals("grados", rep.getSuggestions().get(1).getComment());
        assertEquals("1.ºC", rep.getSuggestions().get(2).getText());
        assertEquals("ordinal", rep.getSuggestions().get(2).getComment());
    }

    @Test
    void testFalseDegreeActuallyOrdinal() {
        String text = "En el 1º Campeonato.";

        List<Replacement> replacements = degreeFinder.findList(text);

        assertTrue(replacements.isEmpty());
    }
}
