package es.bvalero.replacer.finder.replacement.custom;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.bvalero.replacer.common.util.ReplacerUtils;
import es.bvalero.replacer.finder.Replacement;
import java.util.List;
import org.junit.jupiter.api.Test;

class CustomReplacementFinderTest {

    @Test
    void testCompleteWord() {
        String replacement = "x";
        String suggestion = "y";
        String text = "Ax x.";

        CustomReplacementFinder customReplacementFinder = CustomReplacementFinder.of(replacement, false, suggestion);
        List<Replacement> replacements = customReplacementFinder.findList(text);

        assertEquals(1, replacements.size());
        assertEquals(3, replacements.get(0).getStart());
        assertEquals(replacement, replacements.get(0).getText());
        assertEquals(replacement, replacements.get(0).getSuggestions().get(0).getText());
        assertEquals(suggestion, replacements.get(0).getSuggestions().get(1).getText());
    }

    @Test
    void testLowerToUpperCase() {
        String replacement = "parís";
        String suggestion = "París";
        String text = "En parís París.";

        CustomReplacementFinder customReplacementFinder = CustomReplacementFinder.of(replacement, true, suggestion);
        List<Replacement> replacements = customReplacementFinder.findList(text);

        assertEquals(1, replacements.size());
        assertEquals(replacement, replacements.get(0).getText());
        assertEquals(replacement, replacements.get(0).getSuggestions().get(0).getText());
        assertEquals(suggestion, replacements.get(0).getSuggestions().get(1).getText());
    }

    @Test
    void testUpperToLowerCase() {
        String replacement = "Enero";
        String suggestion = "enero";
        String text = "En enero Enero.";

        CustomReplacementFinder customReplacementFinder = CustomReplacementFinder.of(replacement, true, suggestion);
        List<Replacement> replacements = customReplacementFinder.findList(text);

        assertEquals(1, replacements.size());
        assertEquals(replacement, replacements.get(0).getText());
        // We return the lowercase and also the uppercase just in case of correct punctuation rules
        assertEquals(2, replacements.get(0).getSuggestions().size());
        assertEquals("Enero", replacements.get(0).getSuggestions().get(0).getText());
        assertEquals("enero", replacements.get(0).getSuggestions().get(1).getText());
    }

    @Test
    void testUpperToUpperCase() {
        String replacement = "Taiwan";
        String suggestion = "Taiwán";
        String text = "En taiwan Taiwan taiwán Taiwán.";

        CustomReplacementFinder customReplacementFinder = CustomReplacementFinder.of(replacement, true, suggestion);
        List<Replacement> replacements = customReplacementFinder.findList(text);

        assertEquals(1, replacements.size());
        assertEquals(replacement, replacements.get(0).getText());
        assertEquals(replacement, replacements.get(0).getSuggestions().get(0).getText());
        assertEquals(suggestion, replacements.get(0).getSuggestions().get(1).getText());
    }

    @Test
    void testLowerToLowerCase() {
        String replacement = "mas";
        String suggestion = "más";
        String text = "En mas Mas más Más.";

        CustomReplacementFinder customReplacementFinder = CustomReplacementFinder.of(replacement, false, suggestion);
        List<Replacement> replacements = customReplacementFinder.findList(text);

        assertEquals(2, replacements.size());

        assertEquals(replacement, replacements.get(0).getText());
        assertEquals(replacement, replacements.get(0).getSuggestions().get(0).getText());
        assertEquals(suggestion, replacements.get(0).getSuggestions().get(1).getText());
        assertEquals(ReplacerUtils.setFirstUpperCase(replacement), replacements.get(1).getText());
        assertEquals(
            ReplacerUtils.setFirstUpperCase(replacement),
            replacements.get(1).getSuggestions().get(0).getText()
        );
        assertEquals(
            ReplacerUtils.setFirstUpperCase(suggestion),
            replacements.get(1).getSuggestions().get(1).getText()
        );
    }

    @Test
    void testCustomReplacementWithDot() {
        String replacement = "Washington D.C.";
        String suggestion = "Washington, D.C.";
        String text = "En Washington D.CX. y Washington D.C. y Madrid";

        CustomReplacementFinder customReplacementFinder = CustomReplacementFinder.of(replacement, false, suggestion);
        List<Replacement> replacements = customReplacementFinder.findList(text);

        assertEquals(1, replacements.size());
        assertEquals(replacement, replacements.get(0).getText());
    }

    @Test
    void testSeveralSuggestions() {
        String replacement = "on line";
        String suggestion = "online, en línea";
        String text = "Curso on line.";

        CustomReplacementFinder customReplacementFinder = CustomReplacementFinder.of(replacement, false, suggestion);
        List<Replacement> replacements = customReplacementFinder.findList(text);

        assertEquals(1, replacements.size());
        assertEquals(replacement, replacements.get(0).getText());
        assertEquals("on line", replacements.get(0).getSuggestions().get(0).getText());
        assertEquals("online", replacements.get(0).getSuggestions().get(1).getText());
        assertEquals("en línea", replacements.get(0).getSuggestions().get(2).getText());
    }
}
