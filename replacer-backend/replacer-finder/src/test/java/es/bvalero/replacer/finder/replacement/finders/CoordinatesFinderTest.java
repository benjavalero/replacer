package es.bvalero.replacer.finder.replacement.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.finder.Replacement;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class CoordinatesFinderTest {

    private final CoordinatesFinder coordinatesFinder = new CoordinatesFinder();

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
        }
    )
    void testNotValidCoordinates(String text, String expected) {
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
        }
    )
    void testValidCoordinates(String text) {
        List<Replacement> replacements = coordinatesFinder.findList(text);
        assertTrue(replacements.isEmpty());
    }
}
