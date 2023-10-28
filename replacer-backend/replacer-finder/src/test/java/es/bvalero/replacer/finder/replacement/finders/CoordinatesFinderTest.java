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

class CoordinatesFinderTest {

    private CoordinatesFinder coordinatesFinder;

    @BeforeEach
    public void setUp() {
        coordinatesFinder = new CoordinatesFinder();
    }

    @ParameterizedTest
    @CsvSource(
        delimiter = '|',
        value = {
            "38º05′08″|38°05′08″",
            "38°05'08″|38°05′08″",
            "38°05′08\"|38°05′08″",
            "38°05'08''|38°05′08″",
            "38º05′08.325″|38°05′08.325″",
            "38º 05′ 08,325″|38°05′08,325″",
            "38º05′08″N|38°05′08″&nbsp;N",
            "38º05′08″ W|38°05′08″&nbsp;O",
            "38º05′08″ norte|38°05′08″&nbsp;N",
            "38º05′ N|38°05′&nbsp;N",
            "-38°05′08\"|-38°05′08″",
            // "38º05′0883829879874892″|38°05′", // TODO
        }
    )
    void testCoordinates(String text, String expected) {
        List<Replacement> replacements = coordinatesFinder.findList(text);
        assertEquals(1, replacements.size());

        Replacement rep = replacements.get(0);
        assertEquals(StandardType.COORDINATES, rep.getType());
        assertEquals(text, rep.getText());
        assertEquals(text, rep.getSuggestions().get(0).getText());
        assertEquals(expected, rep.getSuggestions().get(1).getText());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "38°05′08″",
            "38° 05′ 08″",
            "38°05′08″",
            "38°{{esd}}05′{{esd}}08″",
            "38°{{esd}}05′{{esd}}08″{{esd}}N",
            "38",
            "38°",
            "38°05",
            "38°05′",
            "38°05′08",
            "38°  05′08″",
            "23° 14′ 18.23.42''", // Bad formatted seconds are ignored
            // "38º05′0883829879874892″ N", // TODO
        }
    )
    void testValidCoordinates(String text) {
        List<Replacement> replacements = coordinatesFinder.findList(text);

        assertTrue(replacements.isEmpty());
    }

    @Test
    void testSeveralCoordinates() {
        String text = "At 23º14'18'' with 1978 and 123º0'15'' of latitude and longitude.";
        List<Replacement> replacements = coordinatesFinder.findList(text);

        assertEquals(2, replacements.size());
    }

    @Test
    void testSeveralCoordinatesWithoutSeconds() {
        String text = "At 23º 14' O with 1978 and 123º0'15'' of latitude and longitude.";
        List<Replacement> replacements = coordinatesFinder.findList(text);

        assertEquals(2, replacements.size());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "23o 14' 18''",
            "23º 14` 18''",
            "230º 14' 18''",
            "23º 140' 18''",
            "230512373828282º 14' 18''",
            "23º 14058383822737373' 18''",
        }
    )
    void testFalseNumbersOrSymbols(String text) {
        List<Replacement> replacements = coordinatesFinder.findList(text);

        assertTrue(replacements.isEmpty());
    }
}
