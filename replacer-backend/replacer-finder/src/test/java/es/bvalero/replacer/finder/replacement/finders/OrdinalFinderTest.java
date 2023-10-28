package es.bvalero.replacer.finder.replacement.finders;

import static org.junit.jupiter.api.Assertions.*;

import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.Suggestion;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

@EnableConfigurationProperties(FinderProperties.class)
@SpringBootTest(classes = OrdinalFinder.class)
class OrdinalFinderTest {

    @Autowired
    private OrdinalFinder ordinalFinder;

    @ParameterizedTest
    @CsvSource(
        {
            "2º, 2.º",
            "13º, 13.º",
            "4ª, 4.ª",
            "1er, 1.º",
            "3er, 3.º",
            "2do, 2.º",
            "1ra, 1.ª",
            "3ra, 3.ª",
            "6tos, {{ord|6.|os}}",
            "1er., 1.º",
            "2nda., 2.ª",
            "1o., 1.º",
            "123º, 123.º",
        }
    )
    void testOrdinal(String ordinal, String expected) {
        List<Replacement> replacements = ordinalFinder.findList(ordinal);

        assertEquals(1, replacements.size());
        assertEquals(ordinal, replacements.get(0).getText());
        assertEquals(ordinal, replacements.get(0).getSuggestions().get(0).getText());
        assertEquals(expected, replacements.get(0).getSuggestions().get(1).getText());
        assertEquals(StandardType.ORDINAL, replacements.get(0).getType());
    }

    @ParameterizedTest
    @ValueSource(strings = { "2.º", "3.ª", "25.º" })
    void testValidOrdinal(String ordinal) {
        List<Replacement> replacements = ordinalFinder.findList(ordinal);

        assertTrue(replacements.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = { "1234º", "0º" })
    void testOrdinalNumberNotValid(String ordinal) {
        List<Replacement> replacements = ordinalFinder.findList(ordinal);

        assertTrue(replacements.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = { "El 12" })
    void testOrdinalTruncated(String ordinal) {
        List<Replacement> replacements = ordinalFinder.findList(ordinal);

        assertTrue(replacements.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = { "2ºC", "25.ºF" })
    void testDegreeReplacement(String ordinal) {
        List<Replacement> replacements = ordinalFinder.findList(ordinal);

        assertTrue(replacements.isEmpty());
    }

    @Test
    void testMasculineOrdinalSuggestions() {
        String text = "El 2º caso.";

        List<Replacement> replacements = ordinalFinder.findList(text);

        assertEquals(1, replacements.size());
        Replacement rep = replacements.get(0);
        assertEquals(StandardType.ORDINAL, rep.getType());

        assertEquals(6, rep.getSuggestions().size());
        List<Suggestion> sug = rep.getSuggestions();

        // Original
        assertEquals("2º", sug.get(0).getText());
        assertNull(sug.get(0).getComment());

        // Dot added
        assertEquals("2.º", sug.get(1).getText());
        assertEquals("se escribe punto antes de la o volada", sug.get(1).getComment());

        // Cardinal
        assertEquals("2", sug.get(2).getText());
        assertEquals("cardinal", sug.get(2).getComment());

        // Degrees
        assertEquals("2°", sug.get(3).getText());
        assertEquals("grados", sug.get(3).getComment());

        // Roman
        assertEquals("II", sug.get(4).getText());
        assertEquals("números romanos", sug.get(4).getComment());

        // Text alternatives
        assertEquals("segundo", sug.get(5).getText());
        assertNull(sug.get(5).getComment());
    }

    @Test
    void testFeminineOrdinalSuggestions() {
        String text = "La 4ª parte.";

        List<Replacement> replacements = ordinalFinder.findList(text);

        assertEquals(1, replacements.size());
        Replacement rep = replacements.get(0);
        assertEquals(StandardType.ORDINAL, rep.getType());

        assertEquals(5, rep.getSuggestions().size());
        List<Suggestion> sug = rep.getSuggestions();

        // Original
        assertEquals("4ª", sug.get(0).getText());
        assertNull(sug.get(0).getComment());

        // Dot added
        assertEquals("4.ª", sug.get(1).getText());
        assertEquals("se escribe punto antes de la a volada", sug.get(1).getComment());

        // Cardinal
        assertEquals("4", sug.get(2).getText());
        assertEquals("cardinal", sug.get(2).getComment());

        // Roman
        assertEquals("IV", sug.get(3).getText());
        assertEquals("números romanos", sug.get(3).getComment());

        // Text alternatives
        assertEquals("cuarta", sug.get(4).getText());
        assertNull(sug.get(4).getComment());
    }

    @Test
    void testFractionalSuggestions() {
        String text = "La 11ª parte.";

        List<Replacement> replacements = ordinalFinder.findList(text);

        assertEquals(1, replacements.size());
        Replacement rep = replacements.get(0);
        assertEquals(StandardType.ORDINAL, rep.getType());

        assertEquals(7, rep.getSuggestions().size());
        List<Suggestion> sug = rep.getSuggestions();

        // Original
        assertEquals("11ª", sug.get(0).getText());
        assertNull(sug.get(0).getComment());

        // Dot added
        assertEquals("11.ª", sug.get(1).getText());
        assertEquals("se escribe punto antes de la a volada", sug.get(1).getComment());

        // Cardinal
        assertEquals("11", sug.get(2).getText());
        assertEquals("cardinal", sug.get(2).getComment());

        // Roman
        assertEquals("XI", sug.get(3).getText());
        assertEquals("números romanos", sug.get(3).getComment());

        // Text alternatives
        assertEquals("undécima", sug.get(4).getText());
        assertNull(sug.get(4).getComment());

        assertEquals("decimoprimera", sug.get(5).getText());
        assertNull(sug.get(5).getComment());

        assertEquals("onceava", sug.get(6).getText());
        assertEquals("fraccionario", sug.get(6).getComment());
    }

    @Test
    void testPluralOrdinalSuggestions() {
        String text = "Los 1ros. en llegar.";

        List<Replacement> replacements = ordinalFinder.findList(text);

        assertEquals(1, replacements.size());
        Replacement rep = replacements.get(0);
        assertEquals(StandardType.ORDINAL, rep.getType());

        assertEquals(6, rep.getSuggestions().size());
        List<Suggestion> sug = rep.getSuggestions();

        // Original
        assertEquals("1ros.", sug.get(0).getText());
        assertNull(sug.get(0).getComment());

        // Dot added
        assertEquals("{{ord|1.|os}}", sug.get(1).getText());
        assertNull(sug.get(1).getComment());

        // Cardinal
        assertEquals("1", sug.get(2).getText());
        assertEquals("cardinal", sug.get(2).getComment());

        // Degrees
        assertEquals("1°", sug.get(3).getText());
        assertEquals("grados", sug.get(3).getComment());

        // Roman
        assertEquals("I", sug.get(4).getText());
        assertEquals("números romanos", sug.get(4).getComment());

        // Text alternatives
        assertEquals("primeros", sug.get(5).getText());
        assertNull(sug.get(5).getComment());
    }
}
