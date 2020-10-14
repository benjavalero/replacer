package es.bvalero.replacer.finder.date;

import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class LongDateFinderTest {
    private final LongDateFinder longDateFinder = new LongDateFinder();

    @ParameterizedTest
    @CsvSource(
        {
            "17 de Agosto de 2019, 17 de agosto de 2019",
            "17 de Setiembre de 2019, 17 de septiembre de 2019",
            "17 de Septiembre de 2019, 17 de septiembre de 2019",
            "17 de agosto del 2019, 17 de agosto de 2019",
            "17 de agosto 2019, 17 de agosto de 2019",
            "17 agosto 2019, 17 de agosto de 2019",
            "07 de agosto de 2019, 7 de agosto de 2019",
        }
    )
    void testUppercaseMonth(String date, String expected) {
        List<Replacement> replacements = longDateFinder.findList(date, WikipediaLanguage.SPANISH);

        Assertions.assertEquals(1, replacements.size());
        Assertions.assertEquals(date, replacements.get(0).getText());
        Assertions.assertEquals(expected, replacements.get(0).getSuggestions().get(0).getText());
    }

    @ParameterizedTest
    @ValueSource(strings = { "15 de agosto de 2019", "123 de Agosto de 2019." })
    void testLowercaseMonth(String date) {
        List<Replacement> replacements = longDateFinder.findList(date, WikipediaLanguage.SPANISH);

        Assertions.assertTrue(replacements.isEmpty());
    }
}
