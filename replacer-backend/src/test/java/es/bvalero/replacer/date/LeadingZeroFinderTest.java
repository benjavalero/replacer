package es.bvalero.replacer.date;

import es.bvalero.replacer.finder.Replacement;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class LeadingZeroFinderTest {

    private LeadingZeroFinder leadingZeroFinder = new LeadingZeroFinder();

    @Test
    public void testOneDigitDay() {
        String date = "7 de Agosto de 2019";
        String text = String.format("En %s.", date);

        List<Replacement> replacements = leadingZeroFinder.findReplacements(text);

        Assert.assertTrue(replacements.isEmpty());
    }

    @Test
    public void testLeadingZeroDayUppercaseMonth() {
        String date = "07 de Agosto de 2019";
        String text = String.format("En %s.", date);

        List<Replacement> replacements = leadingZeroFinder.findReplacements(text);

        Assert.assertEquals(1, replacements.size());
        Assert.assertEquals(date, replacements.get(0).getText());
        Assert.assertEquals("7 de agosto de 2019", replacements.get(0).getSuggestions().get(0).getText());
    }

    @Test
    public void testLeadingZeroDayLowercaseMonth() {
        String date = "07 de setiembre de 2019";
        String text = String.format("En %s.", date);

        List<Replacement> replacements = leadingZeroFinder.findReplacements(text);

        Assert.assertEquals(1, replacements.size());
        Assert.assertEquals(date, replacements.get(0).getText());
        Assert.assertEquals("7 de septiembre de 2019", replacements.get(0).getSuggestions().get(0).getText());
    }

    @Test
    public void testDel() {
        String date = "07 De Agosto Del 2019";
        String expected = "7 de agosto de 2019";
        String text = String.format("En %s.", date);

        List<Replacement> replacements = leadingZeroFinder.findReplacements(text);

        Assert.assertEquals(1, replacements.size());
        Assert.assertEquals(date, replacements.get(0).getText());
        Assert.assertEquals(expected, replacements.get(0).getSuggestions().get(0).getText());
    }

}
