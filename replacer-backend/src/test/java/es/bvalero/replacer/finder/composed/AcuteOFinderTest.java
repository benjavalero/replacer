package es.bvalero.replacer.finder.composed;

import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.misspelling.MisspellingComposedFinder;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AcuteOFinderTest {
    private final AcuteOFinder acuteOFinder = new AcuteOFinder();

    @ParameterizedTest
    @ValueSource(strings = { "1 ó 2", "12 ó 3", "1 ó 23" })
    void testAcuteOBetweenNumbers(String text) {
        List<Replacement> replacements = acuteOFinder.findList(text);
        Assertions.assertEquals(1, replacements.size());

        Replacement rep = replacements.get(0);
        Assertions.assertEquals(MisspellingComposedFinder.TYPE_MISSPELLING_COMPOSED, rep.getType());
        Assertions.assertEquals(AcuteOFinder.SUBTYPE_ACUTE_O, rep.getSubtype());
        Assertions.assertEquals(AcuteOFinder.ACUTE_O, rep.getText());
        Assertions.assertEquals(AcuteOFinder.FIX_ACUTE_O, rep.getSuggestions().get(0).getText());
    }

    @ParameterizedTest
    @ValueSource(strings = { "uno ó dos", "1 ó dos", "uno ó 2", "1 o 2" })
    void testAcuteOBetweenLetters(String text) {
        List<Replacement> replacements = acuteOFinder.findList(text);
        Assertions.assertTrue(replacements.isEmpty());
    }
}
