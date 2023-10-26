package es.bvalero.replacer.finder.replacement.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.finder.Replacement;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class AcuteOFinderTest {

    // The CSV source doesn't allow the value to be non-constant
    // even if we take the subtype from the replacement type constants

    private final AcuteOFinder acuteOFinder = new AcuteOFinder();

    @ParameterizedTest
    @CsvSource(
        value = { "1 ó 2", "12 ó 3", "1 ó 23", "uno ó 2", "1 ó dos", "uno ó dos", "m2 ó 23", "En una ó dos veces" }
    )
    void testAcuteO(String text) {
        List<Replacement> replacements = acuteOFinder.findList(text);
        assertEquals(1, replacements.size());

        Replacement rep = replacements.get(0);
        assertEquals(StandardType.ACUTE_O, rep.getType());
        assertEquals(AcuteOFinder.ACUTE_O, rep.getText());
        assertEquals(AcuteOFinder.ACUTE_O, rep.getSuggestions().get(0).getText());
        assertEquals(AcuteOFinder.FIX_ACUTE_O, rep.getSuggestions().get(1).getText());
    }

    @Test
    void testDoubleAcuteO() {
        final String text = "En dós ó tres veces.";

        List<Replacement> replacements = acuteOFinder.findList(text);
        assertEquals(1, replacements.size());

        Replacement rep = replacements.get(0);
        assertEquals(StandardType.ACUTE_O, rep.getType());
        assertEquals(AcuteOFinder.ACUTE_O, rep.getText());
        assertEquals(7, rep.getStart());
        assertEquals(AcuteOFinder.ACUTE_O, rep.getSuggestions().get(0).getText());
        assertEquals(AcuteOFinder.FIX_ACUTE_O, rep.getSuggestions().get(1).getText());
    }

    @ParameterizedTest
    @ValueSource(strings = { "1 o 2", "1 ó.", "1, ó 2", "1  ó  2" })
    void testAcuteONotValid(String text) {
        List<Replacement> replacements = acuteOFinder.findList(text);
        assertTrue(replacements.isEmpty());
    }
}
