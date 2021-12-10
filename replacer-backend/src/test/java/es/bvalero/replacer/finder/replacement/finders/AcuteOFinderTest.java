package es.bvalero.replacer.finder.replacement.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.finder.replacement.Replacement;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class AcuteOFinderTest {

    private final AcuteOFinder acuteOFinder = new AcuteOFinder();

    @ParameterizedTest
    @CsvSource(
        value = {
            "1 ó 2," + AcuteOFinder.SUBTYPE_ACUTE_O_NUMBERS,
            "12 ó 3," + AcuteOFinder.SUBTYPE_ACUTE_O_NUMBERS,
            "1 ó 23," + AcuteOFinder.SUBTYPE_ACUTE_O_NUMBERS,
            "uno ó 2," + AcuteOFinder.SUBTYPE_ACUTE_O_WORDS,
            "1 ó dos," + AcuteOFinder.SUBTYPE_ACUTE_O_WORDS,
            "uno ó dos," + AcuteOFinder.SUBTYPE_ACUTE_O_WORDS,
            "m2 ó 23," + AcuteOFinder.SUBTYPE_ACUTE_O_WORDS,
            "En una ó dos veces," + AcuteOFinder.SUBTYPE_ACUTE_O_WORDS,
        }
    )
    void testAcuteO(String text, String subtype) {
        List<Replacement> replacements = acuteOFinder.findList(text);
        assertEquals(1, replacements.size());

        Replacement rep = replacements.get(0);
        assertEquals(ReplacementKind.MISSPELLING_COMPOSED, rep.getType().getKind());
        assertEquals(subtype, rep.getType().getSubtype());
        assertEquals(AcuteOFinder.ACUTE_O, rep.getText());
        assertEquals(AcuteOFinder.FIX_ACUTE_O, rep.getSuggestions().get(0).getText());
    }

    @Test
    void testDoubleAcuteO() {
        final String text = "En dós ó tres veces.";

        List<Replacement> replacements = acuteOFinder.findList(text);
        assertEquals(1, replacements.size());

        Replacement rep = replacements.get(0);
        assertEquals(ReplacementKind.MISSPELLING_COMPOSED, rep.getType().getKind());
        assertEquals(AcuteOFinder.SUBTYPE_ACUTE_O_WORDS, rep.getType().getSubtype());
        assertEquals(AcuteOFinder.ACUTE_O, rep.getText());
        assertEquals(7, rep.getStart());
        assertEquals(AcuteOFinder.FIX_ACUTE_O, rep.getSuggestions().get(0).getText());
    }

    @ParameterizedTest
    @ValueSource(strings = { "1 o 2", "1 ó." })
    void testAcuteONotValid(String text) {
        List<Replacement> replacements = acuteOFinder.findList(text);
        assertTrue(replacements.isEmpty());
    }
}
