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

        List<ArticleReplacement> articleReplacements =
                customReplacementFinder.findReplacements(text, replacement, suggestion);

        Assert.assertEquals(1, articleReplacements.size());
        Assert.assertEquals(3, articleReplacements.get(0).getStart());
        Assert.assertEquals(replacement, articleReplacements.get(0).getText());
    }

    @Test
    public void testCustomReplacementLowerCaseToUpperCase() {
        String replacement = "parís";
        String suggestion = "París";
        String text = "En parís París.";

        List<ArticleReplacement> articleReplacements =
                customReplacementFinder.findReplacements(text, replacement, suggestion);

        Assert.assertEquals(1, articleReplacements.size());
        Assert.assertEquals(3, articleReplacements.get(0).getStart());
        Assert.assertEquals(replacement, articleReplacements.get(0).getText());
        Assert.assertEquals(suggestion, articleReplacements.get(0).getSuggestions().get(0).getText());
    }

    @Test
    public void testCustomReplacementUpperCaseToLowerCase() {
        String replacement = "Enero";
        String suggestion = "enero";
        String text = "En Enero enero.";

        List<ArticleReplacement> articleReplacements =
                customReplacementFinder.findReplacements(text, replacement, suggestion);

        Assert.assertEquals(1, articleReplacements.size());
        Assert.assertEquals(3, articleReplacements.get(0).getStart());
        Assert.assertEquals(replacement, articleReplacements.get(0).getText());
        Assert.assertEquals(suggestion, articleReplacements.get(0).getSuggestions().get(0).getText());
    }

    @Test
    public void testCustomReplacementUpperCaseToUpperCase() {
        String replacement = "Mas";
        String suggestion = "Más";
        String text = "En Mas Más mas más.";

        List<ArticleReplacement> articleReplacements =
                customReplacementFinder.findReplacements(text, replacement, suggestion);

        Assert.assertEquals(1, articleReplacements.size());
        Assert.assertEquals(3, articleReplacements.get(0).getStart());
        Assert.assertEquals(replacement, articleReplacements.get(0).getText());
        Assert.assertEquals(suggestion, articleReplacements.get(0).getSuggestions().get(0).getText());
    }

    @Test
    public void testCustomReplacementLowerCaseToLowerCase() {
        String replacement = "mas";
        String suggestion = "más";
        String text = "En mas más Mas Más.";

        List<ArticleReplacement> articleReplacements =
                customReplacementFinder.findReplacements(text, replacement, suggestion);

        Assert.assertEquals(2, articleReplacements.size());

        Assert.assertEquals(3, articleReplacements.get(0).getStart());
        Assert.assertEquals(replacement, articleReplacements.get(0).getText());
        Assert.assertEquals(suggestion, articleReplacements.get(0).getSuggestions().get(0).getText());

        Assert.assertEquals(11, articleReplacements.get(1).getStart());
        Assert.assertEquals(ReplacementFinder.setFirstUpperCase(replacement), articleReplacements.get(1).getText());
        Assert.assertEquals(ReplacementFinder.setFirstUpperCase(suggestion), articleReplacements.get(1).getSuggestions().get(0).getText());
    }

}
