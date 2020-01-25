package es.bvalero.replacer.date;

import es.bvalero.replacer.finder.Replacement;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class UppercaseMonthWithoutDayFinderTest {

    private UppercaseMonthWithoutDayFinder uppercaseMonthWithoutDayFinder = new UppercaseMonthWithoutDayFinder();

    @Test
    public void testUppercaseMonthUppercaseWord() {
        String date = "Desde Agosto de 2019";
        String expected = "Desde agosto de 2019";
        String text = String.format("- %s.", date);

        List<Replacement> replacements = uppercaseMonthWithoutDayFinder.findList(text);

        Assert.assertEquals(1, replacements.size());
        Assert.assertEquals(date, replacements.get(0).getText());
        Assert.assertEquals(expected, replacements.get(0).getSuggestions().get(0).getText());
    }

    @Test
    public void testUppercaseMonthLowercaseWord() {
        String date = "hasta Agosto de 2019";
        String expected = "hasta agosto de 2019";
        String text = String.format("- %s.", date);

        List<Replacement> replacements = uppercaseMonthWithoutDayFinder.findList(text);

        Assert.assertEquals(1, replacements.size());
        Assert.assertEquals(date, replacements.get(0).getText());
        Assert.assertEquals(expected, replacements.get(0).getSuggestions().get(0).getText());
    }

    @Test
    public void testLowercaseMonth() {
        String date = "De agosto de 2019";
        String text = String.format("- %s.", date);

        List<Replacement> replacements = uppercaseMonthWithoutDayFinder.findList(text);

        Assert.assertTrue(replacements.isEmpty());
    }

    @Test
    public void testSetiembre() {
        String date = "En Setiembre de 2019";
        String expected = "En septiembre de 2019";
        String text = String.format("- %s.", date);

        List<Replacement> replacements = uppercaseMonthWithoutDayFinder.findList(text);

        Assert.assertEquals(1, replacements.size());
        Assert.assertEquals(date, replacements.get(0).getText());
        Assert.assertEquals(expected, replacements.get(0).getSuggestions().get(0).getText());
    }

    @Test
    public void testDel() {
        String date = "Desde Agosto Del 2019";
        String expected = "Desde agosto de 2019";
        String text = String.format("- %s.", date);

        List<Replacement> replacements = uppercaseMonthWithoutDayFinder.findList(text);

        Assert.assertEquals(1, replacements.size());
        Assert.assertEquals(date, replacements.get(0).getText());
        Assert.assertEquals(expected, replacements.get(0).getSuggestions().get(0).getText());
    }

}
