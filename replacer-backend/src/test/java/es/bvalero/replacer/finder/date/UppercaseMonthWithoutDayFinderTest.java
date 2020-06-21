package es.bvalero.replacer.finder.date;

import es.bvalero.replacer.finder.Replacement;
import java.util.List;

import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UppercaseMonthWithoutDayFinderTest {
    private final UppercaseMonthWithoutDayFinder uppercaseMonthWithoutDayFinder = new UppercaseMonthWithoutDayFinder();

    @Test
    void testUppercaseMonthUppercaseWord() {
        String date = "Desde Agosto de 2019";
        String expected = "Desde agosto de 2019";
        String text = String.format("- %s.", date);

        List<Replacement> replacements = uppercaseMonthWithoutDayFinder.findList(text, WikipediaLanguage.SPANISH);

        Assertions.assertEquals(1, replacements.size());
        Assertions.assertEquals(date, replacements.get(0).getText());
        Assertions.assertEquals(expected, replacements.get(0).getSuggestions().get(0).getText());
    }

    @Test
    void testUppercaseMonthLowercaseWord() {
        String date = "hasta Agosto de 2019";
        String expected = "hasta agosto de 2019";
        String text = String.format("- %s.", date);

        List<Replacement> replacements = uppercaseMonthWithoutDayFinder.findList(text, WikipediaLanguage.SPANISH);

        Assertions.assertEquals(1, replacements.size());
        Assertions.assertEquals(date, replacements.get(0).getText());
        Assertions.assertEquals(expected, replacements.get(0).getSuggestions().get(0).getText());
    }

    @Test
    void testLowercaseMonth() {
        String date = "De agosto de 2019";
        String text = String.format("- %s.", date);

        List<Replacement> replacements = uppercaseMonthWithoutDayFinder.findList(text, WikipediaLanguage.SPANISH);

        Assertions.assertTrue(replacements.isEmpty());
    }

    @Test
    void testSetiembre() {
        String date = "En Setiembre de 2019";
        String expected = "En septiembre de 2019";
        String text = String.format("- %s.", date);

        List<Replacement> replacements = uppercaseMonthWithoutDayFinder.findList(text, WikipediaLanguage.SPANISH);

        Assertions.assertEquals(1, replacements.size());
        Assertions.assertEquals(date, replacements.get(0).getText());
        Assertions.assertEquals(expected, replacements.get(0).getSuggestions().get(0).getText());
    }

    @Test
    void testDel() {
        String date = "Desde Agosto Del 2019";
        String expected = "Desde agosto de 2019";
        String text = String.format("- %s.", date);

        List<Replacement> replacements = uppercaseMonthWithoutDayFinder.findList(text, WikipediaLanguage.SPANISH);

        Assertions.assertEquals(1, replacements.size());
        Assertions.assertEquals(date, replacements.get(0).getText());
        Assertions.assertEquals(expected, replacements.get(0).getSuggestions().get(0).getText());
    }
}
