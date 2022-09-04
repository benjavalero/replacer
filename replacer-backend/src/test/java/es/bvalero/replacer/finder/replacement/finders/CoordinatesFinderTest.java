package es.bvalero.replacer.finder.replacement.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.common.domain.Replacement;
import es.bvalero.replacer.common.domain.ReplacementType;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class CoordinatesFinderTest {

    private final CoordinatesFinder coordinatesFinder = new CoordinatesFinder();

    @ParameterizedTest
    @CsvSource(
        delimiter = '*',
        value = {
            "19°42′25″N 101°12′16.8″O * {{Coord|19|42|25|N|101|12|16.8|W}}",
            "19°42′25″N 101°12′16,8″O * {{Coord|19|42|25|N|101|12|16.8|W}}",
            "19º42'25\"N 101º12´16.8''E * {{Coord|19|42|25|N|101|12|16.8|E}}",
            "19°42′25″N  101°12′16.8″O * {{Coord|19|42|25|N|101|12|16.8|W}}",
            "19° 42′ 25″ N{{esd}}101° 12′ 16.8″ E * {{Coord|19|42|25|N|101|12|16.8|E}}",
        }
    )
    void testCompleteCoordinates(String text, String expected) {
        List<Replacement> replacements = coordinatesFinder.findList(text);
        assertEquals(1, replacements.size());

        Replacement rep = replacements.get(0);
        assertEquals(ReplacementType.COORDINATES, rep.getType());
        assertEquals(text, rep.getText());
        assertEquals(text, rep.getSuggestions().get(0).getText());
        assertEquals(expected, rep.getSuggestions().get(1).getText());
    }

    @ParameterizedTest
    @ValueSource(strings = { "19°42′25″N" })
    void testFalsePositiveCoordinates(String text) {
        List<Replacement> replacements = coordinatesFinder.findList(text);
        assertTrue(replacements.isEmpty());
    }
}
