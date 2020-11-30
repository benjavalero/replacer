package es.bvalero.replacer.finder;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CustomReplacementFinderTest {

    @Test
    void testCustomReplacement() {
        String replacement = "x";
        String suggestion = "y";
        String text = String.format("Ax %s.", replacement);

        CustomReplacementFinder customReplacementFinder = new CustomReplacementFinder(replacement, suggestion);
        List<Replacement> replacements = customReplacementFinder.findList(text);

        Assertions.assertEquals(1, replacements.size());
        Assertions.assertEquals(3, replacements.get(0).getStart());
        Assertions.assertEquals(replacement, replacements.get(0).getText());
    }

    @Test
    void testCustomReplacementLowerCaseToUpperCase() {
        String replacement = "parís";
        String suggestion = "París";
        String text = "En parís París.";

        CustomReplacementFinder customReplacementFinder = new CustomReplacementFinder(replacement, suggestion);
        List<Replacement> replacements = customReplacementFinder.findList(text);

        Assertions.assertEquals(1, replacements.size());
        Assertions.assertEquals(3, replacements.get(0).getStart());
        Assertions.assertEquals(replacement, replacements.get(0).getText());
        Assertions.assertEquals(suggestion, replacements.get(0).getSuggestions().get(0).getText());
    }

    @Test
    void testCustomReplacementUpperCaseToLowerCase() {
        String replacement = "Enero";
        String suggestion = "enero";
        String text = "En Enero enero.";

        CustomReplacementFinder customReplacementFinder = new CustomReplacementFinder(replacement, suggestion);
        List<Replacement> replacements = customReplacementFinder.findList(text);

        Assertions.assertEquals(1, replacements.size());
        Assertions.assertEquals(3, replacements.get(0).getStart());
        Assertions.assertEquals(replacement, replacements.get(0).getText());
        Assertions.assertEquals(suggestion, replacements.get(0).getSuggestions().get(0).getText());
    }

    @Test
    void testCustomReplacementUpperCaseToUpperCase() {
        String replacement = "Mas";
        String suggestion = "Más";
        String text = "En Mas Más mas más.";

        CustomReplacementFinder customReplacementFinder = new CustomReplacementFinder(replacement, suggestion);
        List<Replacement> replacements = customReplacementFinder.findList(text);

        Assertions.assertEquals(1, replacements.size());
        Assertions.assertEquals(3, replacements.get(0).getStart());
        Assertions.assertEquals(replacement, replacements.get(0).getText());
        Assertions.assertEquals(suggestion, replacements.get(0).getSuggestions().get(0).getText());
    }

    @Test
    void testCustomReplacementLowerCaseToLowerCase() {
        String replacement = "mas";
        String suggestion = "más";
        String text = "En mas más Mas Más.";

        CustomReplacementFinder customReplacementFinder = new CustomReplacementFinder(replacement, suggestion);
        List<Replacement> replacements = customReplacementFinder.findList(text);

        Assertions.assertEquals(2, replacements.size());

        Assertions.assertEquals(3, replacements.get(0).getStart());
        Assertions.assertEquals(replacement, replacements.get(0).getText());
        Assertions.assertEquals(suggestion, replacements.get(0).getSuggestions().get(0).getText());

        Assertions.assertEquals(11, replacements.get(1).getStart());
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
