package es.bvalero.replacer.date;

import es.bvalero.replacer.finder.Replacement;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class UppercaseMonthFinderTest {

    private UppercaseMonthFinder uppercaseMonthFinder = new UppercaseMonthFinder();

    @Test
    public void testUppercaseMonth() {
        String date = "15 de Agosto de 2019";
        String text = String.format("En %s.", date);

        List<Replacement> replacements = uppercaseMonthFinder.findList(text);

        Assert.assertEquals(1, replacements.size());
        Assert.assertEquals(date, replacements.get(0).getText());
        Assert.assertEquals(date.toLowerCase(), replacements.get(0).getSuggestions().get(0).getText());
    }

    @Test
    public void testLowercaseMonth() {
        String date = "15 de agosto de 2019";
        String text = String.format("En %s.", date);

        List<Replacement> replacements = uppercaseMonthFinder.findList(text);

        Assert.assertTrue(replacements.isEmpty());
    }

    @Test
    public void testSetiembre() {
        String date = "17 de Setiembre de 2019";
        String text = String.format("En %s.", date);

        List<Replacement> replacements = uppercaseMonthFinder.findList(text);

        Assert.assertEquals(1, replacements.size());
        Assert.assertEquals(date, replacements.get(0).getText());
        Assert.assertEquals("17 de septiembre de 2019", replacements.get(0).getSuggestions().get(0).getText());
    }

    @Test
    public void testSeptiembre() {
        String date = "17 de Septiembre de 2019";
        String text = String.format("En %s.", date);

        List<Replacement> replacements = uppercaseMonthFinder.findList(text);

        Assert.assertEquals(1, replacements.size());
        Assert.assertEquals(date, replacements.get(0).getText());
        Assert.assertEquals(date.toLowerCase(), replacements.get(0).getSuggestions().get(0).getText());
    }

    @Test
    public void testDel() {
        String date = "17 de Agosto Del 2019";
        String expected = "17 de agosto de 2019";
        String text = String.format("En %s.", date);

        List<Replacement> replacements = uppercaseMonthFinder.findList(text);

        Assert.assertEquals(1, replacements.size());
        Assert.assertEquals(date, replacements.get(0).getText());
        Assert.assertEquals(expected, replacements.get(0).getSuggestions().get(0).getText());
    }

    @Test
    public void testDe() {
        String date = "17 De Agosto De 2019";
        String expected = "17 de agosto de 2019";
        String text = String.format("En %s.", date);

        List<Replacement> replacements = uppercaseMonthFinder.findList(text);

        Assert.assertEquals(1, replacements.size());
        Assert.assertEquals(date, replacements.get(0).getText());
        Assert.assertEquals(expected, replacements.get(0).getSuggestions().get(0).getText());
    }

}
