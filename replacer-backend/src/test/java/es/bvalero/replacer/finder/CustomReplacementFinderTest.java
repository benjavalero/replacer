package es.bvalero.replacer.finder;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class CustomReplacementFinderTest {

    private CustomReplacementFinder customReplacementFinder = new CustomReplacementFinder();

    @Test
    public void testCustomReplacement() {
        String replacement = "x";
        String suggestion = "y";
        String text = String.format("Ax %s.", replacement);

        List<Replacement> replacements =
                customReplacementFinder.findReplacements(text, replacement, suggestion);

        Assert.assertEquals(1, replacements.size());
        Assert.assertEquals(3, replacements.get(0).getStart());
        Assert.assertEquals(replacement, replacements.get(0).getText());
    }

    @Test
    public void testCustomReplacementLowerCaseToUpperCase() {
        String replacement = "parís";
        String suggestion = "París";
        String text = "En parís París.";

        List<Replacement> replacements =
                customReplacementFinder.findReplacements(text, replacement, suggestion);

        Assert.assertEquals(1, replacements.size());
        Assert.assertEquals(3, replacements.get(0).getStart());
        Assert.assertEquals(replacement, replacements.get(0).getText());
        Assert.assertEquals(suggestion, replacements.get(0).getSuggestions().get(0).getText());
    }

    @Test
    public void testCustomReplacementUpperCaseToLowerCase() {
        String replacement = "Enero";
        String suggestion = "enero";
        String text = "En Enero enero.";

        List<Replacement> replacements =
                customReplacementFinder.findReplacements(text, replacement, suggestion);

        Assert.assertEquals(1, replacements.size());
        Assert.assertEquals(3, replacements.get(0).getStart());
        Assert.assertEquals(replacement, replacements.get(0).getText());
        Assert.assertEquals(suggestion, replacements.get(0).getSuggestions().get(0).getText());
    }

    @Test
    public void testCustomReplacementUpperCaseToUpperCase() {
        String replacement = "Mas";
        String suggestion = "Más";
        String text = "En Mas Más mas más.";

        List<Replacement> replacements =
                customReplacementFinder.findReplacements(text, replacement, suggestion);

        Assert.assertEquals(1, replacements.size());
        Assert.assertEquals(3, replacements.get(0).getStart());
        Assert.assertEquals(replacement, replacements.get(0).getText());
        Assert.assertEquals(suggestion, replacements.get(0).getSuggestions().get(0).getText());
    }

    @Test
    public void testCustomReplacementLowerCaseToLowerCase() {
        String replacement = "mas";
        String suggestion = "más";
        String text = "En mas más Mas Más.";

        List<Replacement> replacements =
                customReplacementFinder.findReplacements(text, replacement, suggestion);

        Assert.assertEquals(2, replacements.size());

        Assert.assertEquals(3, replacements.get(0).getStart());
        Assert.assertEquals(replacement, replacements.get(0).getText());
        Assert.assertEquals(suggestion, replacements.get(0).getSuggestions().get(0).getText());

        Assert.assertEquals(11, replacements.get(1).getStart());
        Assert.assertEquals(BaseReplacementFinder.setFirstUpperCase(replacement), replacements.get(1).getText());
        Assert.assertEquals(BaseReplacementFinder.setFirstUpperCase(suggestion), replacements.get(1).getSuggestions().get(0).getText());
    }

}
