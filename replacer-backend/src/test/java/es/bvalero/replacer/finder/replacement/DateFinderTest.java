package es.bvalero.replacer.finder.replacement;

import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.config.XmlConfiguration;
import es.bvalero.replacer.finder.FinderPage;
import java.util.List;
import org.apache.commons.collections4.IterableUtils;
import org.junit.jupiter.api.Assertions;
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
        {
            "7 de Agosto de 2019, 7 de agosto de 2019, " + DateFinder.SUBTYPE_UPPERCASE,
            "17 de Agosto de 2019, 17 de agosto de 2019, " + DateFinder.SUBTYPE_UPPERCASE,
            "07 de Agosto de 2019, 7 de agosto de 2019, " + DateFinder.SUBTYPE_UPPERCASE,
            "17 de Agosto del 2019, 17 de agosto del 2019, " + DateFinder.SUBTYPE_UPPERCASE,
            "17 de Agosto de 2.019, 17 de agosto de 2019, " + DateFinder.SUBTYPE_UPPERCASE,
            "17 Agosto de 2019, 17 de agosto de 2019, " + DateFinder.SUBTYPE_UPPERCASE,
            "17 de Agosto 2019, 17 de agosto de 2019, " + DateFinder.SUBTYPE_UPPERCASE,
            "17 Agosto 2019, 17 de agosto de 2019, " + DateFinder.SUBTYPE_UPPERCASE,
            "17 de Setiembre de 2019, 17 de septiembre de 2019, " + DateFinder.SUBTYPE_UPPERCASE,
            "07 de agosto de 2019, 7 de agosto de 2019, " + DateFinder.SUBTYPE_LEADING_ZERO,
            "07 de agosto del 2019, 7 de agosto del 2019, " + DateFinder.SUBTYPE_LEADING_ZERO,
            "07 agosto de 2019, 7 de agosto de 2019, " + DateFinder.SUBTYPE_LEADING_ZERO,
            "07 de agosto 2019, 7 de agosto de 2019, " + DateFinder.SUBTYPE_LEADING_ZERO,
            "07 agosto 2019, 7 de agosto de 2019, " + DateFinder.SUBTYPE_LEADING_ZERO,
            "07 de agosto de 2.019, 7 de agosto de 2019, " + DateFinder.SUBTYPE_LEADING_ZERO,
            "07 de setiembre de 2019, 7 de septiembre de 2019, " + DateFinder.SUBTYPE_LEADING_ZERO,
            "17 de agosto 2019, 17 de agosto de 2019, " + DateFinder.SUBTYPE_INCOMPLETE,
            "17 agosto de 2019, 17 de agosto de 2019, " + DateFinder.SUBTYPE_INCOMPLETE,
            "17 agosto del 2019, 17 de agosto del 2019, " + DateFinder.SUBTYPE_INCOMPLETE,
            "17 agosto 2019, 17 de agosto de 2019, " + DateFinder.SUBTYPE_INCOMPLETE,
            "17 agosto 2.019, 17 de agosto de 2019, " + DateFinder.SUBTYPE_INCOMPLETE,
            "17 setiembre 2019, 17 de septiembre de 2019, " + DateFinder.SUBTYPE_INCOMPLETE,
            "17 de agosto de 2.019, 17 de agosto de 2019, " + DateFinder.SUBTYPE_DOT_YEAR,
            "17 de agosto del 2.019, 17 de agosto del 2019, " + DateFinder.SUBTYPE_DOT_YEAR,
            "17 de setiembre de 2.019, 17 de septiembre de 2019, " + DateFinder.SUBTYPE_DOT_YEAR,
        }
    )
    void testLongDate(String date, String expected, String subtype) {
        List<Replacement> replacements = dateFinder.findList(date);

        Assertions.assertEquals(1, replacements.size());
        Assertions.assertEquals(date, replacements.get(0).getText());
        Assertions.assertEquals(expected, replacements.get(0).getSuggestions().get(0).getText());
        Assertions.assertEquals(subtype, replacements.get(0).getSubtype());
    }

    @ParameterizedTest
    @ValueSource(strings = { "15 de agosto del 2019", "15 de setiembre de 2020" })
    void testValidLongDate(String date) {
        List<Replacement> replacements = dateFinder.findList(date);

        Assertions.assertTrue(replacements.isEmpty());
    }

    @ParameterizedTest
    @CsvSource(
        {
            "Desde Agosto de 2019, Desde agosto de 2019, " + DateFinder.SUBTYPE_UPPERCASE,
            "desde Agosto de 2019, desde agosto de 2019, " + DateFinder.SUBTYPE_UPPERCASE,
            "de Agosto de 2019, de agosto de 2019, " + DateFinder.SUBTYPE_UPPERCASE,
            "desde Agosto del 2019, desde agosto del 2019, " + DateFinder.SUBTYPE_UPPERCASE,
            "desde Agosto de 2.019, desde agosto de 2019, " + DateFinder.SUBTYPE_UPPERCASE,
            "desde Agosto 2019, desde agosto de 2019, " + DateFinder.SUBTYPE_UPPERCASE,
            "de Setiembre de 2019, de septiembre de 2019, " + DateFinder.SUBTYPE_UPPERCASE,
            "Desde agosto 2019, Desde agosto de 2019, " + DateFinder.SUBTYPE_INCOMPLETE,
            "desde agosto 2.019, desde agosto de 2019, " + DateFinder.SUBTYPE_INCOMPLETE,
            "Desde agosto de 2.019, Desde agosto de 2019, " + DateFinder.SUBTYPE_DOT_YEAR,
            "Desde agosto del 2.019, Desde agosto del 2019, " + DateFinder.SUBTYPE_DOT_YEAR,
        }
    )
    void testMonthYear(String date, String expected, String subtype) {
        List<Replacement> replacements = dateFinder.findList(date);

        Assertions.assertEquals(1, replacements.size());
        Assertions.assertEquals(date, replacements.get(0).getText());
        Assertions.assertEquals(expected, replacements.get(0).getSuggestions().get(0).getText());
        Assertions.assertEquals(subtype, replacements.get(0).getSubtype());
    }

    @ParameterizedTest
    @ValueSource(strings = { "Desde agosto del 2019", "desde setiembre de 2020", "de agosto de 2000" })
    void testValidMonthYear(String date) {
        List<Replacement> replacements = dateFinder.findList(date);

        Assertions.assertTrue(replacements.isEmpty());
    }

    @Test
    void testGalicianMonth() {
        String date = "30 decembro do 1591";
        String expected = "30 de decembro do 1591";
        String subtype = DateFinder.SUBTYPE_INCOMPLETE;

        FinderPage page = FinderPage.of(WikipediaLanguage.GALICIAN, date, "");
        List<Replacement> replacements = IterableUtils.toList(dateFinder.find(page));

        Assertions.assertEquals(1, replacements.size());
        Assertions.assertEquals(date, replacements.get(0).getText());
        Assertions.assertEquals(expected, replacements.get(0).getSuggestions().get(0).getText());
        Assertions.assertEquals(subtype, replacements.get(0).getSubtype());
    }
}
