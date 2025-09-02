package es.bvalero.replacer.finder.replacement.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.StandardType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import es.bvalero.replacer.finder.Suggestion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class CenturyFinderTest {

    private CenturyFinder centuryFinder;

    @BeforeEach
    public void setUp() {
        centuryFinder = new CenturyFinder();
    }

    @ParameterizedTest
    @CsvSource(
        delimiter = '*',
        value = {
            // Lowercase
            "siglo XX * {{siglo|XX||s}}",
            "siglo xx * {{siglo|XX||s}}",
            // Uppercase
            "Siglo XX * {{Siglo|XX||S}}, {{Siglo|XX||s}}",
            // Lowercase with link
            "[[siglo XX]] * {{siglo|XX||s}}, {{siglo|XX||s|1}}",
            // Uppercase with link
            "[[Siglo XX]] * {{Siglo|XX||S}}, {{Siglo|XX||S|1}}, {{Siglo|XX||s}}, {{Siglo|XX||s|1}}",
            // With era
            "siglo II a. C. * {{siglo|II|a|s}}",
            "siglo V d.C. * {{siglo|V|d|s}}",
            "siglo I&nbsp;a.&nbsp;C. * {{siglo|I|a|s}}",
            // With era and with link
            "[[siglo VI d.&nbsp;C.]] * {{siglo|VI|d|s}}, {{siglo|VI|d|s|1}}"
        }
    )
    void testCenturySimple(String century, String expected) {
        List<Replacement> replacements = centuryFinder.findList(century);
        assertEquals(1, replacements.size());

        Replacement rep = replacements.getFirst();
        assertEquals(StandardType.CENTURY, rep.getType());
        assertEquals(century, rep.getText());

        List<String> expectedSuggestions = new ArrayList<>();
        expectedSuggestions.add(century);
        expectedSuggestions.addAll(Arrays.stream(expected.split(",")).map(String::trim).toList());
        assertEquals(expectedSuggestions, rep.getSuggestions().stream().map(Suggestion::getText).toList());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            // Century arabic number
            "siglo 20",
            "Siglo 21",
            // FIXME "siglos XX y XXI",
            // Not whitespace after century word
            "siglo  XX",
            "siglo-XX",
            // Broken link
            "[[siglo XX",
        }
    )
    void testCenturyNotValid(String text) {
        List<Replacement> replacements = centuryFinder.findList(text);

        assertTrue(replacements.isEmpty());
    }

/*
    @Test
    void testCenturyWithSimpleCenturyAfter() {
        String text = "Entre el siglo XIX y principios del XX.";

        List<Replacement> replacements = centuryFinder.findList(text);
        assertEquals(1, replacements.size());

        Replacement rep = replacements.getFirst();
        assertEquals(StandardType.CENTURY, rep.getType());
        assertEquals("siglo XIX y principios del XX", rep.getText());
        assertEquals("{{siglo|XIX||s}} y principios del {{Siglo|XX}}", rep.getSuggestions().get(1).getText());
    }

    @Test
    void testCenturyWithFakeCenturyAfter() {
        String text = "En el siglo XI Alfonso VI...";

        List<Replacement> replacements = centuryFinder.findList(text);
        assertEquals(1, replacements.size());

        Replacement rep = replacements.getFirst();
        assertEquals(StandardType.CENTURY, rep.getType());
        assertEquals("siglo XI", rep.getText());
        assertEquals("{{siglo|XI||s}}", rep.getSuggestions().get(1).getText());
    }

    @Test
    void testCenturyWithCompleteCenturyAfter() {
        String text = "Entre el siglo XIX y el siglo XX.";

        List<Replacement> replacements = centuryFinder.findList(text);

        assertEquals(2, replacements.size());
        assertEquals("siglo XIX", replacements.get(0).getText());
        assertEquals("siglo XX", replacements.get(1).getText());
    }

    @Test
    void testCenturyWithCenturyAfterTooFar() {
        String text = "Entre el siglo XIX y los comienzos del XX.";

        List<Replacement> replacements = centuryFinder.findList(text);

        assertEquals(1, replacements.size());
        assertEquals("siglo XIX", replacements.getFirst().getText());
    }

    @Test
    void testCenturyWithCompleteCenturyAfterInvalid() {
        String text = "Entre el siglo XIX y principios del siglo XXL";

        List<Replacement> replacements = centuryFinder.findList(text);

        assertEquals(1, replacements.size());
        assertEquals("siglo XIX", replacements.getFirst().getText());
    }

    @Test
    void testCenturyAfterFalsePositive() {
        String text = "Los siglos y el siglo XIX.";

        List<Replacement> replacements = centuryFinder.findList(text);

        assertEquals(1, replacements.size());
        assertEquals("siglo XIX", replacements.getFirst().getText());
    }

     */
}
