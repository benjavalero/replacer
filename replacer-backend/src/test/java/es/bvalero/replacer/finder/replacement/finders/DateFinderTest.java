package es.bvalero.replacer.finder.replacement.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.common.domain.Replacement;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.config.XmlConfiguration;
import java.util.List;
import org.apache.commons.collections4.IterableUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = { DateFinder.class, XmlConfiguration.class })
class DateFinderTest {

    @Autowired
    private DateFinder dateFinder;

    @ParameterizedTest
    @CsvSource(
        delimiter = '|',
        value = {
            "7 de Agosto de 2019|7 de agosto de 2019",
            "7 de  Agosto 2019|7 de agosto de 2019",
            "17 de Agosto de 2019|17 de agosto de 2019",
            "07 de Agosto de 2019|7 de agosto de 2019",
            "17 de Agosto del 2019|17 de agosto de 2019",
            "17 de Agosto de 2.019|17 de agosto de 2019",
            "17 Agosto de 2019|17 de agosto de 2019",
            "17 de Agosto 2019|17 de agosto de 2019",
            "17 Agosto 2019|17 de agosto de 2019",
            "17 de Setiembre de 2019|17 de septiembre de 2019",
            "07 de agosto de 2019|7 de agosto de 2019",
            "07 de agosto del 2019|7 de agosto de 2019",
            "07 agosto de 2019|7 de agosto de 2019",
            "07 de agosto 2019|7 de agosto de 2019",
            "07 agosto 2019|7 de agosto de 2019",
            "07 de agosto de 2.019|7 de agosto de 2019",
            "07 de setiembre de 2019|7 de septiembre de 2019",
            "17 de agosto 2019|17 de agosto de 2019",
            "17 agosto de 2019|17 de agosto de 2019",
            "17 agosto del 2019|17 de agosto de 2019",
            "17 agosto 2019|17 de agosto de 2019",
            "17 agosto 2.019|17 de agosto de 2019",
            "17 setiembre 2019|17 de septiembre de 2019",
            "17 de agosto de 2.019|17 de agosto de 2019",
            "17 de agosto del 2.019|17 de agosto de 2019",
            "17 de setiembre de 2.019|17 de septiembre de 2019",
            "10 de septiembre, 2022|10 de septiembre de 2022",
        }
    )
    void testLongDate(String date, String expected) {
        List<Replacement> replacements = dateFinder.findList(date);

        assertEquals(1, replacements.size());
        assertEquals(date, replacements.get(0).getText());
        assertEquals(date, replacements.get(0).getSuggestions().get(0).getText());
        assertEquals(expected, replacements.get(0).getSuggestions().get(1).getText());
        assertEquals(ReplacementType.DATE, replacements.get(0).getType());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "15 de agosto del 2019",
            "15 de setiembre de 2020",
            "el 22 de agosto de 2022",
            "Siendo el 31 de agosto de 2022,",
            "siendo el 31 de agosto de 2022.",
        }
    )
    void testValidLongDate(String date) {
        List<Replacement> replacements = dateFinder.findList(date);

        assertTrue(replacements.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "el 22 de agostos de 2022",
            "([[Condado de Mayo]]).",
            "la “Revolución de Noviembre”.\n",
            "Feria de Abril ...",
            "Feria de Mayo ... ",
            "0 de septiembre de 2020",
            "7-Agosto-2019",
            "4 de septiembre, 3150",
            "10, septiembre de 2022",
        }
    )
    void testNotDate(String date) {
        List<Replacement> replacements = dateFinder.findList(date);

        assertTrue(replacements.isEmpty());
    }

    @ParameterizedTest
    @CsvSource(
        {
            "Desde Agosto de 2019, Desde agosto de 2019",
            "desde Agosto de 2019, desde agosto de 2019",
            "desde Agosto del 2019, desde agosto de 2019",
            "desde Agosto de 2.019, desde agosto de 2019",
            "desde Agosto 2019, desde agosto de 2019",
            "Desde agosto 2019, Desde agosto de 2019",
            "desde agosto 2.019, desde agosto de 2019",
            "Desde agosto de 2.019, Desde agosto de 2019",
            "En agosto del 2.019, En agosto de 2019",
        }
    )
    void testMonthYear(String date, String expected) {
        List<Replacement> replacements = dateFinder.findList(date);

        assertEquals(1, replacements.size());
        assertEquals(date, replacements.get(0).getText());
        assertEquals(date, replacements.get(0).getSuggestions().get(0).getText());
        assertEquals(expected, replacements.get(0).getSuggestions().get(1).getText());
        assertEquals(ReplacementType.DATE, replacements.get(0).getType());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "Desde agosto del 2019",
            "desde setiembre de 2020",
            "de Agosto de 2019",
            "de agosto de 2000",
            "En agosto de 2000",
            "en agosto de 2000",
        }
    )
    void testValidMonthYear(String date) {
        List<Replacement> replacements = dateFinder.findList(date);

        assertTrue(replacements.isEmpty());
    }

    @Test
    void testGalicianMonth() {
        String date = "30 decembro do 1591";
        String expected = "30 de decembro do 1591";

        WikipediaPage page = WikipediaPage.of(WikipediaLanguage.GALICIAN, date, "");
        List<Replacement> replacements = IterableUtils.toList(dateFinder.find(page));

        assertEquals(1, replacements.size());
        assertEquals(date, replacements.get(0).getText());
        assertEquals(date, replacements.get(0).getSuggestions().get(0).getText());
        assertEquals(expected, replacements.get(0).getSuggestions().get(1).getText());
        assertEquals(ReplacementType.DATE, replacements.get(0).getType());
    }

    @ParameterizedTest
    @CsvSource(
        delimiter = '|',
        value = {
            "Mayo 3, 1999|3 de mayo de 1999",
            "Mayo 3 1999|3 de mayo de 1999",
            "Octubre 14 de 2006|14 de octubre de 2006",
            "Octubre 14 del 2006|14 de octubre de 2006",
            "Mayo 03, 1999|3 de mayo de 1999",
            "Mayo 3, 1.999|3 de mayo de 1999",
            "mayo 3, 1999|3 de mayo de 1999",
        }
    )
    void testUnorderedDateMonthDayYear(String date, String expected) {
        List<Replacement> replacements = dateFinder.findList(date);

        assertEquals(1, replacements.size());
        assertEquals(date, replacements.get(0).getText());
        assertEquals(date, replacements.get(0).getSuggestions().get(0).getText());
        assertEquals(expected, replacements.get(0).getSuggestions().get(1).getText());
        assertEquals(ReplacementType.DATE, replacements.get(0).getType());
    }

    @ParameterizedTest
    @CsvSource(
        delimiter = '|',
        value = {
            "Siendo mayo 3, 1999. | mayo 3, 1999 | 3 de mayo de 1999",
            "[[Line]]. * Mayo 3, 1999. | Mayo 3, 1999 | 3 de mayo de 1999",
        }
    )
    void testUnorderedDateMonthDayYearWithWordBefore(String text, String match, String fix) {
        List<Replacement> replacements = dateFinder.findList(text);

        assertEquals(1, replacements.size());
        assertEquals(match, replacements.get(0).getText());
        assertEquals(match, replacements.get(0).getSuggestions().get(0).getText());
        assertEquals(fix, replacements.get(0).getSuggestions().get(1).getText());
        assertEquals(ReplacementType.DATE, replacements.get(0).getType());
    }

    @ParameterizedTest
    @CsvSource(
        delimiter = '|',
        value = {
            "2013, Octubre 30|30 de octubre de 2013",
            "2013 Octubre 30|30 de octubre de 2013",
            "2013, octubre 30|30 de octubre de 2013",
            "2013, Mayo 4|4 de mayo de 2013",
            "2013, Mayo 04|4 de mayo de 2013",
            "2.013, Mayo 4|4 de mayo de 2013",
        }
    )
    void testUnorderedDateYearMonthDay(String date, String expected) {
        List<Replacement> replacements = dateFinder.findList(date);

        assertEquals(1, replacements.size());
        assertEquals(date, replacements.get(0).getText());
        assertEquals(date, replacements.get(0).getSuggestions().get(0).getText());
        assertEquals(expected, replacements.get(0).getSuggestions().get(1).getText());
        assertEquals(ReplacementType.DATE, replacements.get(0).getType());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "el 15 de agosto del 2019",
            "El 15 de setiembre de 2020",
            "por 22 de agosto de 2022",
            "en 22 de agosto de 2022",
            "En 22 de agosto de 2022",
        }
    )
    void testValidDateWithValidArticle(String date) {
        List<Replacement> replacements = dateFinder.findList(date);

        assertTrue(replacements.isEmpty());
    }

    @ParameterizedTest
    @CsvSource(
        delimiter = '|',
        value = {
            "en 22 de Agosto de 2022|el 22 de agosto de 2022|en 22 de agosto de 2022",
            "En Agosto 22, 2022|El 22 de agosto de 2022|En 22 de agosto de 2022",
            "A 2022, Agosto 22|Al 22 de agosto de 2022|A 22 de agosto de 2022",
        }
    )
    void testNotValidDateWithWrongArticle(String date, String expected1, String expected2) {
        List<Replacement> replacements = dateFinder.findList(date);

        assertEquals(1, replacements.size());
        assertEquals(date, replacements.get(0).getText());
        assertEquals(3, replacements.get(0).getSuggestions().size());
        assertEquals(date, replacements.get(0).getSuggestions().get(0).getText());
        assertEquals(expected1, replacements.get(0).getSuggestions().get(1).getText());
        assertEquals(expected2, replacements.get(0).getSuggestions().get(2).getText());
        assertEquals(ReplacementType.DATE, replacements.get(0).getType());
    }
}
