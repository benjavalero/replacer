package es.bvalero.replacer.finder;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CustomReplacementFinderTest {

    @Test
    void testCompleteWord() {
        String replacement = "x";
        String suggestion = "y";
        String text = "Ax x.";

        CustomReplacementFinder customReplacementFinder = new CustomReplacementFinder(replacement, suggestion);
        List<Replacement> replacements = customReplacementFinder.findList(text);

        Assertions.assertEquals(1, replacements.size());
        Assertions.assertEquals(3, replacements.get(0).getStart());
        Assertions.assertEquals(replacement, replacements.get(0).getText());
        Assertions.assertEquals(suggestion, replacements.get(0).getSuggestions().get(0).getText());
    }

    @Test
    void testLowerToUpperCase() {
        String replacement = "parís";
        String suggestion = "París";
        String text = "En parís París.";

        CustomReplacementFinder customReplacementFinder = new CustomReplacementFinder(replacement, suggestion);
        List<Replacement> replacements = customReplacementFinder.findList(text);

        Assertions.assertEquals(1, replacements.size());
        Assertions.assertEquals(replacement, replacements.get(0).getText());
        Assertions.assertEquals(suggestion, replacements.get(0).getSuggestions().get(0).getText());
    }

    @Test
    void testUpperToLowerCase() {
        String replacement = "Enero";
        String suggestion = "enero";
        String text = "En enero Enero.";

        CustomReplacementFinder customReplacementFinder = new CustomReplacementFinder(replacement, suggestion);
        List<Replacement> replacements = customReplacementFinder.findList(text);

        Assertions.assertEquals(1, replacements.size());
        Assertions.assertEquals(replacement, replacements.get(0).getText());
        Assertions.assertEquals(suggestion, replacements.get(0).getSuggestions().get(0).getText());
    }

    @Test
    void testUpperToUpperCase() {
        String replacement = "Taiwan";
        String suggestion = "Taiwán";
        String text = "En taiwan Taiwan taiwán Taiwán.";

        CustomReplacementFinder customReplacementFinder = new CustomReplacementFinder(replacement, suggestion);
        List<Replacement> replacements = customReplacementFinder.findList(text);

        Assertions.assertEquals(1, replacements.size());
        Assertions.assertEquals(replacement, replacements.get(0).getText());
        Assertions.assertEquals(suggestion, replacements.get(0).getSuggestions().get(0).getText());
    }

    @Test
    void testLowerToLowerCase() {
        String replacement = "mas";
        String suggestion = "más";
        String text = "En mas Mas más Más.";

        CustomReplacementFinder customReplacementFinder = new CustomReplacementFinder(replacement, suggestion);
        List<Replacement> replacements = customReplacementFinder.findList(text);

        Assertions.assertEquals(2, replacements.size());

        Assertions.assertEquals(replacement, replacements.get(0).getText());
        Assertions.assertEquals(suggestion, replacements.get(0).getSuggestions().get(0).getText());
        Assertions.assertEquals(FinderUtils.setFirstUpperCase(replacement), replacements.get(1).getText());
        Assertions.assertEquals(
            FinderUtils.setFirstUpperCase(suggestion),
            replacements.get(1).getSuggestions().get(0).getText()
        );
    }

    @Test
    void testCustomReplacementWithDot() {
        String replacement = "Washington D.C.";
        String suggestion = "Washington, D.C.";
        String text = "En Washington D.CX. y Washington D.C. y Madrid";

        CustomReplacementFinder customReplacementFinder = new CustomReplacementFinder(replacement, suggestion);
        List<Replacement> replacements = customReplacementFinder.findList(text);

        Assertions.assertEquals(1, replacements.size());
        Assertions.assertEquals(replacement, replacements.get(0).getText());
    }
}
