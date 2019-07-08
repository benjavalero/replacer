package es.bvalero.replacer.date;

import es.bvalero.replacer.finder.ArticleReplacement;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class DateFinderTest {

    private DateFinder dateFinder = new DateFinder();

    @Test
    public void testDateFinder() {
        String date = "15 de Agosto de 2019";
        String text = String.format("En %s.", date);

        List<ArticleReplacement> articleReplacements = dateFinder.findReplacements(text);

        Assert.assertEquals(1, articleReplacements.size());
        Assert.assertEquals(3, articleReplacements.get(0).getStart());
        Assert.assertEquals(date, articleReplacements.get(0).getText());
        Assert.assertEquals(date.toLowerCase(), articleReplacements.get(0).getSuggestions().get(0).getText());
    }

    @Test
    public void testDateFinderLowerCaseMonth() {
        String date = "15 de agosto de 2019";
        String text = String.format("En %s.", date);

        List<ArticleReplacement> articleReplacements = dateFinder.findReplacements(text);

        Assert.assertTrue(articleReplacements.isEmpty());
    }

    @Test
    public void testDateFinderOneDigitDay() {
        String date = "7 de Agosto de 2019";
        String text = String.format("En %s.", date);

        List<ArticleReplacement> articleReplacements = dateFinder.findReplacements(text);

        Assert.assertEquals(1, articleReplacements.size());
        Assert.assertEquals(3, articleReplacements.get(0).getStart());
        Assert.assertEquals(date, articleReplacements.get(0).getText());
        Assert.assertEquals(date.toLowerCase(), articleReplacements.get(0).getSuggestions().get(0).getText());
    }

    @Test
    public void testDateFinderDayWithZero() {
        String date = "07 de Agosto de 2019";
        String text = String.format("En %s.", date);

        List<ArticleReplacement> articleReplacements = dateFinder.findReplacements(text);

        System.out.println(articleReplacements);
        Assert.assertTrue(articleReplacements.isEmpty());
    }

}
