package es.bvalero.replacer.finder.date;

import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class MonthYearFinderTest {
    private final MonthYearFinder monthYearFinder = new MonthYearFinder();

    @ParameterizedTest
    @CsvSource(
        {
            "Desde Agosto de 2019, Desde agosto de 2019",
            "hasta Agosto de 2019, hasta agosto de 2019",
            "En Setiembre de 2019, En septiembre de 2019",
            "Desde agosto Del 2019, Desde agosto de 2019",
            "Desde agosto 2019, Desde agosto de 2019",
        }
    )
    void testUppercaseMonth(String date, String expected) {
        List<Replacement> replacements = monthYearFinder.findList(date, WikipediaLanguage.SPANISH);

        Assertions.assertEquals(1, replacements.size());
        Assertions.assertEquals(date, replacements.get(0).getText());
        Assertions.assertEquals(expected, replacements.get(0).getSuggestions().get(0).getText());
    }

    @ParameterizedTest
    @ValueSource(strings = { "Desde agosto de 2019", "Donde Agosto de 2019." })
    void testLowercaseMonth(String date) {
        List<Replacement> replacements = monthYearFinder.findList(date, WikipediaLanguage.SPANISH);

        Assertions.assertTrue(replacements.isEmpty());
    }
}
