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
            // With aliased link
            "[[Siglo XX|XX]] * [[Siglo XX|XX]] * {{Siglo|XX}}, {{Siglo|XX|||1}}",
            // Wrapped with template
            "{{esd|Siglo XX}} * {{esd|Siglo XX}} * {{Siglo|XX||S}}, {{Siglo|XX||s}}",
            "{{ac|Siglo XX}} * {{ac|Siglo XX}} * {{Siglo|XX|a|S}}, {{Siglo|XX|a|s}}",
            // With era
            "siglo II a. C. * siglo II a. C. * {{siglo|II|a|s}}",
            "siglo V d.C. * siglo V d.C. * {{siglo|V|d|s}}",
            "siglo I&nbsp;a.&nbsp;C. * siglo I&nbsp;a.&nbsp;C. * {{siglo|I|a|s}}",
            "siglo X d.{{esd}}C. * siglo X d.{{esd}}C. * {{siglo|X|d|s}}",
            "siglo I adC * siglo I adC * {{siglo|I|a|s}}",
            "siglo I a.&nbsp;de&nbsp;C. * siglo I a.&nbsp;de&nbsp;C. * {{siglo|I|a|s}}",
            // With era and with link
            "[[siglo VI d.&nbsp;C.]] * [[siglo VI d.&nbsp;C.]] * {{siglo|VI|d|s}}, {{siglo|VI|d|s|1}}",
            // Broken link
            "[[siglo XX * siglo XX * {{siglo|XX||s}}",
            // With Arabic numbers
            "siglo 20 * siglo 20 * {{siglo|XX||s}}",
            // With fake century after
            "siglo XI Alfonso VI * siglo XI * {{siglo|XI||s}}",
            // With century after too far
            "siglo XIX y en los comienzos del XX * siglo XIX * {{siglo|XIX||s}}",
            // With invalid century after
            "siglo XIX y principios del siglo XXL * siglo XIX * {{siglo|XIX||s}}",
            // After false positive
            "Los siglos y el siglo XIX * siglo XIX * {{siglo|XIX||s}}",
            // Century abbreviated
            "s. XX * s. XX * {{Siglo|XX||a}}",
            // Century abbreviated with hard space
            "S.&nbsp;XIX * S.&nbsp;XIX * {{Siglo|XIX||A}}, {{Siglo|XIX||a}}",
        }
    )
    void testCenturySimple(String text, String century, String expected) {
        List<Replacement> replacements = centuryFinder.findList(text);
        assertEquals(1, replacements.size());

        Replacement rep = replacements.getFirst();
        assertEquals(StandardType.CENTURY, rep.type());
        assertEquals(century, rep.text());

        List<String> expectedSuggestions = new ArrayList<>();
        expectedSuggestions.add(century);
        expectedSuggestions.addAll(Arrays.stream(expected.split(",")).map(String::trim).toList());
        assertEquals(expectedSuggestions, rep.suggestions().stream().map(Suggestion::getText).toList());
    }

    @ParameterizedTest
    @CsvSource(
        delimiter = '*',
        value = {
            // With simple century after
            "siglo XX-XXI * siglo XX, XXI * {{siglo|XX||s}}, {{Siglo|XXI}}",
            "siglos XX-XXI * XX, XXI * {{Siglo|XX}}, {{Siglo|XXI}}",
            "siglo XX o XXI * siglo XX, XXI * {{siglo|XX||s}}, {{Siglo|XXI}}",
            "siglos XX o XXI * XX, XXI * {{Siglo|XX}}, {{Siglo|XXI}}",
            "siglo XIX y los comienzos del XX * siglo XIX, XX * {{siglo|XIX||s}}, {{Siglo|XX}}",
            // With lowercase century after
            "siglos xv y xvi * xv, xvi * {{Siglo|XV}}, {{Siglo|XVI}}",
            // With Arabic numbers
            "siglos 15 y 16 * 15, 16 * {{Siglo|XV}}, {{Siglo|XVI}}",
            // Plural century
            "siglos XX y XXI * XX, XXI * {{Siglo|XX}}, {{Siglo|XXI}}",
            // Plural century with era
            "siglos III a. C. y II d. C. * III a. C., II d. C. * {{Siglo|III|a}}, {{Siglo|II|d}}",
            "siglos III a. C. y II a. C. * III a. C., II a. C. * {{Siglo|III|a}}, {{Siglo|II|a}}",
            // Several extensions (we cannot use a comma to test so we use a semicolon)
            "siglos xv; xvi y xvii * xv, xvi, xvii * {{Siglo|XV}}, {{Siglo|XVI}}, {{Siglo|XVII}}",
            "de los siglos XVII; XVIII y XIX. * XVII, XVIII, XIX * {{Siglo|XVII}}, {{Siglo|XVIII}}, {{Siglo|XIX}}",
            // With century word between
            "Entre el siglo XIX y el siglo XX. * siglo XIX, siglo XX * {{siglo|XIX||s}}, {{siglo|XX||s}}",
            "Entre el s. XIX y el s. XX. * s. XIX, s. XX * {{Siglo|XIX||a}}, {{Siglo|XX||a}}",
        }
    )
    void testCenturyExtended(String text, String century, String expected) {
        List<Replacement> replacements = centuryFinder.findList(text);

        final List<String> expectedCentury = Arrays.stream(century.split(",")).map(String::trim).toList();
        final List<String> expectedSuggestions = Arrays.stream(expected.split(",")).map(String::trim).toList();
        assertEquals(expectedCentury.size(), replacements.size());
        for (int i = 0; i < replacements.size(); i++) {
            Replacement rep = replacements.get(i);
            assertEquals(StandardType.CENTURY, rep.type());
            assertEquals(expectedCentury.get(i), rep.text());
            assertTrue(rep.suggestions().size() > 1);
            assertEquals(expectedSuggestions.get(i), rep.suggestions().get(1).getText());
        }
    }

    @Test
    void testCenturyExtendedWithNewLine() {
        String text =
            """
            Siglo XIX |
            21
            """;

        List<Replacement> replacements = centuryFinder.findList(text);
        assertEquals(1, replacements.size());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            // Plural century with nothing after
            "siglos XX y nada más después",
            // Not whitespace after century word
            "siglo-XX",
            // Century number immediately after the abbreviation
            "S.I",
            // Wrapped by a greater link
            "[[Terremotos en el siglo XX]]",
            "[[Siglo XX en España]]",
            "[[Terremotos en el siglo XX en España]]",
            "[[Siglo XX|s. XX]]",
            // Already fixed
            "{{Siglo|XX}}",
            "{{Siglo|I|a}}",
            "{{siglo|XVI||s}}",
        }
    )
    void testCenturyNotValid(String text) {
        List<Replacement> replacements = centuryFinder.findList(text);

        assertTrue(replacements.isEmpty());
    }
}
