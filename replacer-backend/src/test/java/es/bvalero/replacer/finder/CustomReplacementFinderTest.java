package es.bvalero.replacer.finder;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class CustomReplacementFinderTest {

    @Test
    public void testCustomReplacement() {
        String replacement = "x";
        String suggestion = "y";
        String text = String.format("Ax %s.", replacement);

        CustomReplacementFinder customReplacementFinder = new CustomReplacementFinder(replacement, suggestion);
        List<Replacement> replacements = customReplacementFinder.findList(text);

        Assert.assertEquals(1, replacements.size());
        Assert.assertEquals(3, replacements.get(0).getStart());
        Assert.assertEquals(replacement, replacements.get(0).getText());
    }

    @Test
    public void testCustomReplacementLowerCaseToUpperCase() {
        String replacement = "parís";
        String suggestion = "París";
        String text = "En parís París.";

        CustomReplacementFinder customReplacementFinder = new CustomReplacementFinder(replacement, suggestion);
        List<Replacement> replacements = customReplacementFinder.findList(text);

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

        CustomReplacementFinder customReplacementFinder = new CustomReplacementFinder(replacement, suggestion);
        List<Replacement> replacements = customReplacementFinder.findList(text);

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

        CustomReplacementFinder customReplacementFinder = new CustomReplacementFinder(replacement, suggestion);
        List<Replacement> replacements = customReplacementFinder.findList(text);

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

        CustomReplacementFinder customReplacementFinder = new CustomReplacementFinder(replacement, suggestion);
        List<Replacement> replacements = customReplacementFinder.findList(text);

        Assert.assertEquals(2, replacements.size());

        Assert.assertEquals(3, replacements.get(0).getStart());
        Assert.assertEquals(replacement, replacements.get(0).getText());
        Assert.assertEquals(suggestion, replacements.get(0).getSuggestions().get(0).getText());

        Assert.assertEquals(11, replacements.get(1).getStart());
        Assert.assertEquals(FinderUtils.setFirstUpperCase(replacement), replacements.get(1).getText());
        Assert.assertEquals(
            FinderUtils.setFirstUpperCase(suggestion),
            replacements.get(1).getSuggestions().get(0).getText()
        );
    }
}
