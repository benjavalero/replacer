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

    // The CSV source doesn't allow the value to be non-constant
    // even if we take the subtype from the replacement type constants
    private static final String SUBTYPE_DOT_YEAR = "Año con punto";
    private static final String SUBTYPE_INCOMPLETE = "Fecha incompleta";
    private static final String SUBTYPE_LEADING_ZERO = "Día con cero";
    private static final String SUBTYPE_UPPERCASE = "Mes en mayúscula";
    private static final String SUBTYPE_UNORDERED = "Fecha desordenada";

    @Autowired
    private DateFinder dateFinder;

    @ParameterizedTest
    @CsvSource(
        delimiter = '|',
        value = {
            "7 de Agosto de 2019|7 de agosto de 2019|" + SUBTYPE_UPPERCASE,
            "7 de  Agosto 2019|7 de agosto de 2019|" + SUBTYPE_UPPERCASE,
            "17 de Agosto de 2019|17 de agosto de 2019|" + SUBTYPE_UPPERCASE,
            "07 de Agosto de 2019|7 de agosto de 2019|" + SUBTYPE_UPPERCASE,
            "17 de Agosto del 2019|17 de agosto del 2019|" + SUBTYPE_UPPERCASE,
            "17 de Agosto de 2.019|17 de agosto de 2019|" + SUBTYPE_UPPERCASE,
            "17 Agosto de 2019|17 de agosto de 2019|" + SUBTYPE_UPPERCASE,
            "17 de Agosto 2019|17 de agosto de 2019|" + SUBTYPE_UPPERCASE,
            "17 Agosto 2019|17 de agosto de 2019|" + SUBTYPE_UPPERCASE,
            "17 de Setiembre de 2019|17 de septiembre de 2019|" + SUBTYPE_UPPERCASE,
            "07 de agosto de 2019|7 de agosto de 2019|" + SUBTYPE_LEADING_ZERO,
            "07 de agosto del 2019|7 de agosto del 2019|" + SUBTYPE_LEADING_ZERO,
            "07 agosto de 2019|7 de agosto de 2019|" + SUBTYPE_LEADING_ZERO,
            "07 de agosto 2019|7 de agosto de 2019|" + SUBTYPE_LEADING_ZERO,
            "07 agosto 2019|7 de agosto de 2019|" + SUBTYPE_LEADING_ZERO,
            "07 de agosto de 2.019|7 de agosto de 2019|" + SUBTYPE_LEADING_ZERO,
            "07 de setiembre de 2019|7 de septiembre de 2019|" + SUBTYPE_LEADING_ZERO,
            "17 de agosto 2019|17 de agosto de 2019|" + SUBTYPE_INCOMPLETE,
            "17 agosto de 2019|17 de agosto de 2019|" + SUBTYPE_INCOMPLETE,
            "17 agosto del 2019|17 de agosto del 2019|" + SUBTYPE_INCOMPLETE,
            "17 agosto 2019|17 de agosto de 2019|" + SUBTYPE_INCOMPLETE,
            "17 agosto 2.019|17 de agosto de 2019|" + SUBTYPE_DOT_YEAR,
            "17 setiembre 2019|17 de septiembre de 2019|" + SUBTYPE_INCOMPLETE,
            "17 de agosto de 2.019|17 de agosto de 2019|" + SUBTYPE_DOT_YEAR,
            "17 de agosto del 2.019|17 de agosto del 2019|" + SUBTYPE_DOT_YEAR,
            "17 de setiembre de 2.019|17 de septiembre de 2019|" + SUBTYPE_DOT_YEAR,
        }
    )
    void testLongDate(String date, String expected, String subtype) {
        List<Replacement> replacements = dateFinder.findList(date);

        assertEquals(1, replacements.size());
        assertEquals(date, replacements.get(0).getText());
        assertEquals(date, replacements.get(0).getSuggestions().get(0).getText());
        assertEquals(expected, replacements.get(0).getSuggestions().get(1).getText());
        assertEquals(subtype, replacements.get(0).getType().getSubtype());
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
        }
    )
    void testNotDate(String date) {
        List<Replacement> replacements = dateFinder.findList(date);

        assertTrue(replacements.isEmpty());
    }

    @ParameterizedTest
    @CsvSource(
        {
            "Desde Agosto de 2019, Desde agosto de 2019, " + SUBTYPE_UPPERCASE,
            "desde Agosto de 2019, desde agosto de 2019, " + SUBTYPE_UPPERCASE,
            "de Agosto de 2019, de agosto de 2019, " + SUBTYPE_UPPERCASE,
            "desde Agosto del 2019, desde agosto del 2019, " + SUBTYPE_UPPERCASE,
            "desde Agosto de 2.019, desde agosto de 2019, " + SUBTYPE_UPPERCASE,
            "desde Agosto 2019, desde agosto de 2019, " + SUBTYPE_UPPERCASE,
            "de Setiembre de 2019, de septiembre de 2019, " + SUBTYPE_UPPERCASE,
            "Desde agosto 2019, Desde agosto de 2019, " + SUBTYPE_INCOMPLETE,
            "desde agosto 2.019, desde agosto de 2019, " + SUBTYPE_DOT_YEAR,
            "Desde agosto de 2.019, Desde agosto de 2019, " + SUBTYPE_DOT_YEAR,
            "En agosto del 2.019, En agosto del 2019, " + SUBTYPE_DOT_YEAR,
        }
    )
    void testMonthYear(String date, String expected, String subtype) {
        List<Replacement> replacements = dateFinder.findList(date);

        assertEquals(1, replacements.size());
        assertEquals(date, replacements.get(0).getText());
        assertEquals(date, replacements.get(0).getSuggestions().get(0).getText());
        assertEquals(expected, replacements.get(0).getSuggestions().get(1).getText());
        assertEquals(subtype, replacements.get(0).getType().getSubtype());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "Desde agosto del 2019",
            "desde setiembre de 2020",
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
        assertEquals(SUBTYPE_INCOMPLETE, replacements.get(0).getType().getSubtype());
    }

    @ParameterizedTest
    @CsvSource(
        delimiter = '|',
        value = {
            "Mayo 3, 1999|3 de mayo de 1999|" + SUBTYPE_UNORDERED,
            "Mayo 3 1999|3 de mayo de 1999|" + SUBTYPE_UNORDERED,
            "Octubre 14 de 2006|14 de octubre de 2006|" + SUBTYPE_UNORDERED,
            "Octubre 14 del 2006|14 de octubre del 2006|" + SUBTYPE_UNORDERED,
            "Mayo 03, 1999|3 de mayo de 1999|" + SUBTYPE_UNORDERED,
            "Mayo 3, 1.999|3 de mayo de 1999|" + SUBTYPE_UNORDERED,
            "mayo 3, 1999|3 de mayo de 1999|" + SUBTYPE_UNORDERED,
        }
    )
    void testUnorderedDateMonthDayYear(String date, String expected, String subtype) {
        List<Replacement> replacements = dateFinder.findList(date);

        assertEquals(1, replacements.size());
        assertEquals(date, replacements.get(0).getText());
        assertEquals(date, replacements.get(0).getSuggestions().get(0).getText());
        assertEquals(expected, replacements.get(0).getSuggestions().get(1).getText());
        assertEquals(subtype, replacements.get(0).getType().getSubtype());
    }

    @Test
    void testUnorderedDateMonthDayYearWithText() {
        String text = "Siendo mayo 3, 1999.";
        String match = "mayo 3, 1999";
        String fix = "3 de mayo de 1999";
        List<Replacement> replacements = dateFinder.findList(text);

        assertEquals(1, replacements.size());
        assertEquals(match, replacements.get(0).getText());
        assertEquals(match, replacements.get(0).getSuggestions().get(0).getText());
        assertEquals(fix, replacements.get(0).getSuggestions().get(1).getText());
        assertEquals(ReplacementType.DATE_UNORDERED, replacements.get(0).getType());
    }

    @ParameterizedTest
    @CsvSource(
        delimiter = '|',
        value = {
            "2013, Octubre 30|30 de octubre de 2013|" + SUBTYPE_UNORDERED,
            "2013 Octubre 30|30 de octubre de 2013|" + SUBTYPE_UNORDERED,
            "2013, octubre 30|30 de octubre de 2013|" + SUBTYPE_UNORDERED,
            "2013, Mayo 4|4 de mayo de 2013|" + SUBTYPE_UNORDERED,
            "2013, Mayo 04|4 de mayo de 2013|" + SUBTYPE_UNORDERED,
            "2.013, Mayo 4|4 de mayo de 2013|" + SUBTYPE_UNORDERED,
        }
    )
    void testUnorderedDateYearMonthDay(String date, String expected, String subtype) {
        List<Replacement> replacements = dateFinder.findList(date);

        assertEquals(1, replacements.size());
        assertEquals(date, replacements.get(0).getText());
        assertEquals(date, replacements.get(0).getSuggestions().get(0).getText());
        assertEquals(expected, replacements.get(0).getSuggestions().get(1).getText());
        assertEquals(subtype, replacements.get(0).getType().getSubtype());
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
            "en 22 de Agosto de 2022|el 22 de agosto de 2022|en 22 de agosto de 2022|" + SUBTYPE_UPPERCASE,
            "En Agosto 22, 2022|El 22 de agosto de 2022|En 22 de agosto de 2022|" + SUBTYPE_UNORDERED,
            "A 2022, Agosto 22|Al 22 de agosto de 2022|A 22 de agosto de 2022|" + SUBTYPE_UNORDERED,
        }
    )
    void testNotValidDateWithWrongArticle(String date, String expected1, String expected2, String subtype) {
        List<Replacement> replacements = dateFinder.findList(date);

        assertEquals(1, replacements.size());
        assertEquals(date, replacements.get(0).getText());
        assertEquals(3, replacements.get(0).getSuggestions().size());
        assertEquals(date, replacements.get(0).getSuggestions().get(0).getText());
        assertEquals(expected1, replacements.get(0).getSuggestions().get(1).getText());
        assertEquals(expected2, replacements.get(0).getSuggestions().get(2).getText());
        assertEquals(subtype, replacements.get(0).getType().getSubtype());
    }
}
