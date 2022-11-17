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

class CenturyFinderTest {

    private final CenturyFinder centuryFinder = new CenturyFinder();

    @ParameterizedTest
    @CsvSource(
        value = {
            "El siglo XX., siglo XX, {{siglo|XX||s}}",
            "El siglo V., siglo V, {{siglo|V||s}}",
            "El siglo XXI., siglo XXI, {{siglo|XXI||s}}",
            "El siglo XIII., siglo XIII, {{siglo|XIII||s}}",
            "El siglo xx., siglo xx, {{siglo|XX||s}}",
        }
    )
    void testCenturyLowerCase(String text, String century, String expected) {
        List<Replacement> replacements = centuryFinder.findList(text);
        assertEquals(1, replacements.size());

        Replacement rep = replacements.get(0);
        assertEquals(ReplacementType.CENTURY, rep.getType());
        assertEquals(century, rep.getText());
        assertEquals(century, rep.getSuggestions().get(0).getText());
        assertEquals(expected, rep.getSuggestions().get(1).getText());
    }

    @ParameterizedTest
    @CsvSource(value = { "El Siglo XX., Siglo XX, {{Siglo|XX||S}}, {{Siglo|XX||s}}" })
    void testCenturyUpperCase(String text, String century, String upper, String lower) {
        List<Replacement> replacements = centuryFinder.findList(text);
        assertEquals(1, replacements.size());

        Replacement rep = replacements.get(0);
        assertEquals(ReplacementType.CENTURY, rep.getType());
        assertEquals(century, rep.getText());
        assertEquals(century, rep.getSuggestions().get(0).getText());
        assertEquals(upper, rep.getSuggestions().get(1).getText());
        assertEquals(lower, rep.getSuggestions().get(2).getText());
    }

    @ParameterizedTest
    @CsvSource(value = { "El [[siglo XX]]., [[siglo XX]], {{siglo|XX||s}}, {{siglo|XX||s|1}}" })
    void testCenturyLowerCaseWithLink(String text, String century, String notLinked, String linked) {
        List<Replacement> replacements = centuryFinder.findList(text);
        assertEquals(1, replacements.size());

        Replacement rep = replacements.get(0);
        assertEquals(ReplacementType.CENTURY, rep.getType());
        assertEquals(century, rep.getText());
        assertEquals(century, rep.getSuggestions().get(0).getText());
        assertEquals(notLinked, rep.getSuggestions().get(1).getText());
        assertEquals(linked, rep.getSuggestions().get(2).getText());
    }

    @ParameterizedTest
    @CsvSource(
        value = {
            "El [[Siglo XX]]., [[Siglo XX]], {{Siglo|XX||S}}, {{Siglo|XX||S|1}}, {{Siglo|XX||s}}, {{Siglo|XX||s|1}}",
        }
    )
    void testCenturyUpperCaseWithLink(
        String text,
        String century,
        String upperNotLinked,
        String upperLinked,
        String lowerNotLinked,
        String lowerLinked
    ) {
        List<Replacement> replacements = centuryFinder.findList(text);
        assertEquals(1, replacements.size());

        Replacement rep = replacements.get(0);
        assertEquals(ReplacementType.CENTURY, rep.getType());
        assertEquals(century, rep.getText());
        assertEquals(century, rep.getSuggestions().get(0).getText());
        assertEquals(upperNotLinked, rep.getSuggestions().get(1).getText());
        assertEquals(upperLinked, rep.getSuggestions().get(2).getText());
        assertEquals(lowerNotLinked, rep.getSuggestions().get(3).getText());
        assertEquals(lowerLinked, rep.getSuggestions().get(4).getText());
    }

    @ParameterizedTest
    @ValueSource(strings = { "El siglo 20.", "Siglo 21.", "Los siglos XX y XXI.", "El siglo  XX." })
    void testCenturyNotValid(String text) {
        List<Replacement> replacements = centuryFinder.findList(text);
        assertTrue(replacements.isEmpty());
    }

    @ParameterizedTest
    @CsvSource(
        value = {
            "El siglo II a. C., siglo II a. C., {{siglo|II|a|s}}",
            "El siglo V d.C., siglo V d.C., {{siglo|V|d|s}}",
            "El siglo I&nbsp;a.&nbsp;C., siglo I&nbsp;a.&nbsp;C., {{siglo|I|a|s}}",
        }
    )
    void testCenturyWithEra(String text, String century, String expected) {
        List<Replacement> replacements = centuryFinder.findList(text);
        assertEquals(1, replacements.size());

        Replacement rep = replacements.get(0);
        assertEquals(ReplacementType.CENTURY, rep.getType());
        assertEquals(century, rep.getText());
        assertEquals(century, rep.getSuggestions().get(0).getText());
        assertEquals(expected, rep.getSuggestions().get(1).getText());
    }

    @ParameterizedTest
    @CsvSource(value = { "El [[siglo VI d.&nbsp;C.]], [[siglo VI d.&nbsp;C.]], {{siglo|VI|d|s}}, {{siglo|VI|d|s|1}}" })
    void testCenturyWithEraLinked(String text, String century, String notLinked, String linked) {
        List<Replacement> replacements = centuryFinder.findList(text);
        assertEquals(1, replacements.size());

        Replacement rep = replacements.get(0);
        assertEquals(ReplacementType.CENTURY, rep.getType());
        assertEquals(century, rep.getText());
        assertEquals(century, rep.getSuggestions().get(0).getText());
        assertEquals(notLinked, rep.getSuggestions().get(1).getText());
        assertEquals(linked, rep.getSuggestions().get(2).getText());
    }

    @Test
    void testCenturyWithSimpleCenturyAfter() {
        String text = "Entre el siglo XIX y principios del XX.";

        List<Replacement> replacements = centuryFinder.findList(text);

        assertEquals(1, replacements.size());
        Replacement rep = replacements.get(0);
        assertEquals(ReplacementType.CENTURY, rep.getType());
        assertEquals("siglo XIX y principios del XX", rep.getText());
        assertEquals("{{siglo|XIX||s}} y principios del {{Siglo|XX}}", rep.getSuggestions().get(1).getText());
    }

    @Test
    void testCenturyWithCompleteCenturyAfter() {
        String text = "Entre el siglo XIX y principios del siglo XX.";

        List<Replacement> replacements = centuryFinder.findList(text);

        assertEquals(2, replacements.size());
        assertEquals("siglo XIX", replacements.get(0).getText());
        assertEquals("siglo XX", replacements.get(1).getText());
    }
}
