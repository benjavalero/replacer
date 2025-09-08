package es.bvalero.replacer.finder.replacement.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.StandardType;
import es.bvalero.replacer.finder.Suggestion;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
            "siglo XX * siglo XX * {{siglo|XX||s}}",
            "siglo xx * siglo xx * {{siglo|XX||s}}",
            // With hard space or several spaces
            "siglo&nbsp;XX * siglo&nbsp;XX * {{siglo|XX||s}}",
            "siglo  XX * siglo  XX * {{siglo|XX||s}}",
            // Uppercase
            "Siglo XX * Siglo XX * {{Siglo|XX||S}}, {{Siglo|XX||s}}",
            // Lowercase with link
            "[[siglo XX]] * [[siglo XX]] * {{siglo|XX||s}}, {{siglo|XX||s|1}}",
            // Uppercase with link
            "[[Siglo XX]] * [[Siglo XX]] * {{Siglo|XX||S}}, {{Siglo|XX||S|1}}, {{Siglo|XX||s}}, {{Siglo|XX||s|1}}",
            // With era
            "siglo II a. C. * siglo II a. C. * {{siglo|II|a|s}}",
            "siglo V d.C. * siglo V d.C. * {{siglo|V|d|s}}",
            "siglo I&nbsp;a.&nbsp;C. * siglo I&nbsp;a.&nbsp;C. * {{siglo|I|a|s}}",
            "siglo X d.{{esd}}C. * siglo X d.{{esd}}C. * {{siglo|X|d|s}}",
            // With era and with link
            "[[siglo VI d.&nbsp;C.]] * [[siglo VI d.&nbsp;C.]] * {{siglo|VI|d|s}}, {{siglo|VI|d|s|1}}",
            // With simple century after
            "siglo XX-XXI * siglo XX-XXI * {{siglo|XX||s}}-{{Siglo|XXI}}",
            "siglos XX-XXI * siglos XX-XXI * siglos {{Siglo|XX}}-{{Siglo|XXI}}",
            "siglo XX o XXI * siglo XX o XXI * {{siglo|XX||s}} o {{Siglo|XXI}}",
            "siglos XX o XXI * siglos XX o XXI * siglos {{Siglo|XX}} o {{Siglo|XXI}}",
            "siglo XIX y principios del XX * siglo XIX y principios del XX * {{siglo|XIX||s}} y principios del {{Siglo|XX}}",
            // With fake century after
            "siglo XI Alfonso VI * siglo XI * {{siglo|XI||s}}",
            // With century after too far
            "siglo XIX y los comienzos del XX * siglo XIX * {{siglo|XIX||s}}",
            // With invalid century after
            "siglo XIX y principios del siglo XXL * siglo XIX * {{siglo|XIX||s}}",
            // After false positive
            "Los siglos y el siglo XIX * siglo XIX * {{siglo|XIX||s}}",
            // Plural century
            "siglos XX y XXI * siglos XX y XXI * siglos {{Siglo|XX}} y {{Siglo|XXI}}",
            // Plural century with era
            "siglos I a. C. y II d. C. * siglos I a. C. y II * siglos {{Siglo|I}} a. C. y {{Siglo|II}}",
            // Century abbreviated
            "s. XX * s. XX * {{Siglo|XX||a}}",
            // Century abbreviated with hard space
            "S.&nbsp;XIX * S.&nbsp;XIX * {{Siglo|XIX||A}}, {{Siglo|XIX||a}}",
            /*
        A.C (solo cambia la parte del siglo y deja eso fuera de la plantilla)
        a. C (cuando no hay un punto después de la C)
        a. de C.
        d. de C.
        a.c. (lo deja fuera de la plantilla)
        [[siglo X|X]] (enlace X) (es el que más me encuentro)
        {{AC|siglo X}} (no lo convierte en {{siglo X|a}})
        {{esd|siglo X}} (no lo convierte en {{siglo X|s}})
             */
        }
    )
    void testCenturySimple(String text, String century, String expected) {
        List<Replacement> replacements = centuryFinder.findList(text);
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
            // Plural century with nothing after
            "siglos XX y nada más después",
            // Not whitespace after century word
            "siglo-XX",
            // Broken link
            "[[siglo XX",
        }
    )
    void testCenturyNotValid(String text) {
        List<Replacement> replacements = centuryFinder.findList(text);

        assertTrue(replacements.isEmpty());
    }

    @Test
    void testCenturyWithCompleteCenturyAfter() {
        String text = "Entre el siglo XIX y el siglo XX.";

        List<Replacement> replacements = centuryFinder.findList(text);

        assertEquals(2, replacements.size());
        assertEquals("siglo XIX", replacements.get(0).getText());
        assertEquals("siglo XX", replacements.get(1).getText());
    }
}
