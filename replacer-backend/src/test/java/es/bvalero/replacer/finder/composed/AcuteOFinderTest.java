package es.bvalero.replacer.finder.composed;

import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.misspelling.MisspellingComposedFinder;
import java.util.List;
import org.junit.jupiter.api.Assertions;
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
        Assertions.assertEquals(1, replacements.size());

        Replacement rep = replacements.get(0);
        Assertions.assertEquals(MisspellingComposedFinder.TYPE_MISSPELLING_COMPOSED, rep.getType());
        Assertions.assertEquals(subtype, rep.getSubtype());
        Assertions.assertEquals(AcuteOFinder.ACUTE_O, rep.getText());
        Assertions.assertEquals(AcuteOFinder.FIX_ACUTE_O, rep.getSuggestions().get(0).getText());
    }

    @Test
    void testDoubleAcuteO() {
        final String text = "En dós ó tres veces.";

        List<Replacement> replacements = acuteOFinder.findList(text);
        Assertions.assertEquals(1, replacements.size());

        Replacement rep = replacements.get(0);
        Assertions.assertEquals(MisspellingComposedFinder.TYPE_MISSPELLING_COMPOSED, rep.getType());
        Assertions.assertEquals(AcuteOFinder.SUBTYPE_ACUTE_O_WORDS, rep.getSubtype());
        Assertions.assertEquals(AcuteOFinder.ACUTE_O, rep.getText());
        Assertions.assertEquals(7, rep.getStart());
        Assertions.assertEquals(AcuteOFinder.FIX_ACUTE_O, rep.getSuggestions().get(0).getText());
    }

    @ParameterizedTest
    @ValueSource(strings = { "1 o 2", "1 ó." })
    void testAcuteONotValid(String text) {
        List<Replacement> replacements = acuteOFinder.findList(text);
        Assertions.assertTrue(replacements.isEmpty());
    }
}
