package es.bvalero.replacer.finder.date;

import es.bvalero.replacer.finder.Replacement;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LeadingZeroFinderTest {
    private final LeadingZeroFinder leadingZeroFinder = new LeadingZeroFinder();

    @Test
    public void testOneDigitDay() {
        String date = "7 de Agosto de 2019";
        String text = String.format("En %s.", date);

        List<Replacement> replacements = leadingZeroFinder.findList(text);

        Assertions.assertTrue(replacements.isEmpty());
    }

    @Test
    public void testLeadingZeroDayUppercaseMonth() {
        String date = "07 de Agosto de 2019";
        String text = String.format("En %s.", date);

        List<Replacement> replacements = leadingZeroFinder.findList(text);

        Assertions.assertEquals(1, replacements.size());
        Assertions.assertEquals(date, replacements.get(0).getText());
        Assertions.assertEquals("7 de agosto de 2019", replacements.get(0).getSuggestions().get(0).getText());
    }

    @Test
    public void testLeadingZeroDayLowercaseMonth() {
        String date = "07 de setiembre de 2019";
        String text = String.format("En %s.", date);

        List<Replacement> replacements = leadingZeroFinder.findList(text);

        Assertions.assertEquals(1, replacements.size());
        Assertions.assertEquals(date, replacements.get(0).getText());
        Assertions.assertEquals("7 de septiembre de 2019", replacements.get(0).getSuggestions().get(0).getText());
    }

    @Test
    public void testDel() {
        String date = "07 De Agosto Del 2019";
        String expected = "7 de agosto de 2019";
        String text = String.format("En %s.", date);

        List<Replacement> replacements = leadingZeroFinder.findList(text);

        Assertions.assertEquals(1, replacements.size());
        Assertions.assertEquals(date, replacements.get(0).getText());
        Assertions.assertEquals(expected, replacements.get(0).getSuggestions().get(0).getText());
    }
}
