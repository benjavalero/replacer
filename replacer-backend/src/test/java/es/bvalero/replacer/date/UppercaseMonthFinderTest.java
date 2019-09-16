package es.bvalero.replacer.date;

import es.bvalero.replacer.finder.ArticleReplacement;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class UppercaseMonthFinderTest {

    private UppercaseMonthFinder uppercaseMonthFinder = new UppercaseMonthFinder();

    @Test
    public void testUppercaseMonth() {
        String date = "15 de Agosto de 2019";
        String text = String.format("En %s.", date);

        List<ArticleReplacement> replacements = uppercaseMonthFinder.findReplacements(text);

        Assert.assertEquals(1, replacements.size());
        Assert.assertEquals(date, replacements.get(0).getText());
        Assert.assertEquals(date.toLowerCase(), replacements.get(0).getSuggestions().get(0).getText());
    }

    @Test
    public void testLowercaseMonth() {
        String date = "15 de agosto de 2019";
        String text = String.format("En %s.", date);

        List<ArticleReplacement> replacements = uppercaseMonthFinder.findReplacements(text);

        Assert.assertTrue(replacements.isEmpty());
    }

    @Test
    public void testSetiembre() {
        String date = "17 de Setiembre de 2019";
        String text = String.format("En %s.", date);

        List<ArticleReplacement> articleReplacements = uppercaseMonthFinder.findReplacements(text);

        Assert.assertEquals(1, articleReplacements.size());
        Assert.assertEquals(date, articleReplacements.get(0).getText());
        Assert.assertEquals("17 de septiembre de 2019", articleReplacements.get(0).getSuggestions().get(0).getText());
    }

    @Test
    public void testSeptiembre() {
        String date = "17 de Septiembre de 2019";
        String text = String.format("En %s.", date);

        List<ArticleReplacement> articleReplacements = uppercaseMonthFinder.findReplacements(text);

        Assert.assertEquals(1, articleReplacements.size());
        Assert.assertEquals(date, articleReplacements.get(0).getText());
        Assert.assertEquals(date.toLowerCase(), articleReplacements.get(0).getSuggestions().get(0).getText());
    }

    @Test
    public void testDel() {
        String date = "17 de Agosto del 2019";
        String expected = "17 de agosto de 2019";
        String text = String.format("En %s.", date);

        List<ArticleReplacement> articleReplacements = uppercaseMonthFinder.findReplacements(text);

        Assert.assertEquals(1, articleReplacements.size());
        Assert.assertEquals(date, articleReplacements.get(0).getText());
        Assert.assertEquals(expected, articleReplacements.get(0).getSuggestions().get(0).getText());
    }

}
