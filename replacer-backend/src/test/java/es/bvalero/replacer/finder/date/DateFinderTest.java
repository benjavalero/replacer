package es.bvalero.replacer.finder.date;

import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFinder;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class DateFinderTest {
    private final UppercaseLongDateFinder uppercaseLongDateFinder = new UppercaseLongDateFinder();
    private final LeadingZeroFinder leadingZeroFinder = new LeadingZeroFinder();
    private final IncompleteLongDateFinder incompleteLongDateFinder = new IncompleteLongDateFinder();
    private final DotLongDateFinder dotLongDateFinder = new DotLongDateFinder();
    // Run the finders in reverse orders of priority to test possible nesting
    private final List<ReplacementFinder> longDateFinders = List.of(
        dotLongDateFinder,
        incompleteLongDateFinder,
        leadingZeroFinder,
        uppercaseLongDateFinder
    );
    private final UppercaseMonthYearFinder uppercaseMonthYearFinder = new UppercaseMonthYearFinder();
    private final IncompleteMonthYearFinder incompleteMonthYearFinder = new IncompleteMonthYearFinder();
    private final DotMonthYearFinder dotMonthYearFinder = new DotMonthYearFinder();
    private final List<ReplacementFinder> monthYearFinders = List.of(
        dotMonthYearFinder,
        incompleteMonthYearFinder,
        uppercaseMonthYearFinder
    );

    @ParameterizedTest
    @CsvSource(
        {
            "7 de Agosto de 2019, 7 de agosto de 2019, " + UppercaseLongDateFinder.SUBTYPE_UPPERCASE_LONG_DATE,
            "17 de Agosto de 2019, 17 de agosto de 2019, " + UppercaseLongDateFinder.SUBTYPE_UPPERCASE_LONG_DATE,
            "07 de Agosto de 2019, 7 de agosto de 2019, " + UppercaseLongDateFinder.SUBTYPE_UPPERCASE_LONG_DATE,
            "17 de Agosto del 2019, 17 de agosto del 2019, " + UppercaseLongDateFinder.SUBTYPE_UPPERCASE_LONG_DATE,
            "17 de Agosto de 2.019, 17 de agosto de 2019, " + UppercaseLongDateFinder.SUBTYPE_UPPERCASE_LONG_DATE,
            "17 Agosto de 2019, 17 de agosto de 2019, " + UppercaseLongDateFinder.SUBTYPE_UPPERCASE_LONG_DATE,
            "17 de Agosto 2019, 17 de agosto de 2019, " + UppercaseLongDateFinder.SUBTYPE_UPPERCASE_LONG_DATE,
            "17 Agosto 2019, 17 de agosto de 2019, " + UppercaseLongDateFinder.SUBTYPE_UPPERCASE_LONG_DATE,
            "17 de Setiembre de 2019, 17 de septiembre de 2019, " + UppercaseLongDateFinder.SUBTYPE_UPPERCASE_LONG_DATE,
            "07 de agosto de 2019, 7 de agosto de 2019, " + LeadingZeroFinder.SUBTYPE_LEADING_ZERO,
            "07 de agosto del 2019, 7 de agosto del 2019, " + LeadingZeroFinder.SUBTYPE_LEADING_ZERO,
            "07 agosto de 2019, 7 de agosto de 2019, " + LeadingZeroFinder.SUBTYPE_LEADING_ZERO,
            "07 de agosto 2019, 7 de agosto de 2019, " + LeadingZeroFinder.SUBTYPE_LEADING_ZERO,
            "07 agosto 2019, 7 de agosto de 2019, " + LeadingZeroFinder.SUBTYPE_LEADING_ZERO,
            "07 de agosto de 2.019, 7 de agosto de 2019, " + LeadingZeroFinder.SUBTYPE_LEADING_ZERO,
            "07 de setiembre de 2019, 7 de septiembre de 2019, " + LeadingZeroFinder.SUBTYPE_LEADING_ZERO,
            "17 de agosto 2019, 17 de agosto de 2019, " + IncompleteLongDateFinder.SUBTYPE_INCOMPLETE_LONG_DATE,
            "17 agosto de 2019, 17 de agosto de 2019, " + IncompleteLongDateFinder.SUBTYPE_INCOMPLETE_LONG_DATE,
            "17 agosto del 2019, 17 de agosto del 2019, " + IncompleteLongDateFinder.SUBTYPE_INCOMPLETE_LONG_DATE,
            "17 agosto 2019, 17 de agosto de 2019, " + IncompleteLongDateFinder.SUBTYPE_INCOMPLETE_LONG_DATE,
            "17 agosto 2.019, 17 de agosto de 2019, " + IncompleteLongDateFinder.SUBTYPE_INCOMPLETE_LONG_DATE,
            "17 setiembre 2019, 17 de septiembre de 2019, " + IncompleteLongDateFinder.SUBTYPE_INCOMPLETE_LONG_DATE,
            "17 de agosto de 2.019, 17 de agosto de 2019, " + DotLongDateFinder.SUBTYPE_DOT_LONG_DATE,
            "17 de agosto del 2.019, 17 de agosto del 2019, " + DotLongDateFinder.SUBTYPE_DOT_LONG_DATE,
            "17 de setiembre de 2.019, 17 de septiembre de 2019, " + DotLongDateFinder.SUBTYPE_DOT_LONG_DATE,
        }
    )
    void testLongDate(String date, String expected, String subtype) {
        List<Replacement> replacements = longDateFinders
            .stream()
            .flatMap(finder -> finder.findStream(date, WikipediaLanguage.SPANISH))
            .collect(Collectors.toList());

        Assertions.assertEquals(1, replacements.size());
        Assertions.assertEquals(date, replacements.get(0).getText());
        Assertions.assertEquals(expected, replacements.get(0).getSuggestions().get(0).getText());
        Assertions.assertEquals(subtype, replacements.get(0).getSubtype());
    }

    @ParameterizedTest
    @ValueSource(strings = { "15 de agosto del 2019", "15 de setiembre de 2020" })
    void testValidLongDate(String date) {
        List<Replacement> replacements = longDateFinders
            .stream()
            .flatMap(finder -> finder.findStream(date, WikipediaLanguage.SPANISH))
            .collect(Collectors.toList());

        Assertions.assertTrue(replacements.isEmpty());
    }

    @ParameterizedTest
    @CsvSource(
        {
            "Desde Agosto de 2019, Desde agosto de 2019, " + UppercaseMonthYearFinder.SUBTYPE_UPPERCASE_MONTH_YEAR,
            "desde Agosto de 2019, desde agosto de 2019, " + UppercaseMonthYearFinder.SUBTYPE_UPPERCASE_MONTH_YEAR,
            "de Agosto de 2019, de agosto de 2019, " + UppercaseMonthYearFinder.SUBTYPE_UPPERCASE_MONTH_YEAR,
            "desde Agosto del 2019, desde agosto del 2019, " + UppercaseMonthYearFinder.SUBTYPE_UPPERCASE_MONTH_YEAR,
            "desde Agosto de 2.019, desde agosto de 2019, " + UppercaseMonthYearFinder.SUBTYPE_UPPERCASE_MONTH_YEAR,
            "desde Agosto 2019, desde agosto de 2019, " + UppercaseMonthYearFinder.SUBTYPE_UPPERCASE_MONTH_YEAR,
            "de Setiembre de 2019, de septiembre de 2019, " + UppercaseMonthYearFinder.SUBTYPE_UPPERCASE_MONTH_YEAR,
            "Desde agosto 2019, Desde agosto de 2019, " + IncompleteMonthYearFinder.SUBTYPE_INCOMPLETE_MONTH_YEAR,
            "desde agosto 2.019, desde agosto de 2019, " + IncompleteMonthYearFinder.SUBTYPE_INCOMPLETE_MONTH_YEAR,
            "Desde agosto de 2.019, Desde agosto de 2019, " + DotMonthYearFinder.SUBTYPE_DOT_MONTH_YEAR,
            "Desde agosto del 2.019, Desde agosto del 2019, " + DotMonthYearFinder.SUBTYPE_DOT_MONTH_YEAR,
        }
    )
    void testMonthYear(String date, String expected, String subtype) {
        List<Replacement> replacements = monthYearFinders
            .stream()
            .flatMap(finder -> finder.findStream(date, WikipediaLanguage.SPANISH))
            .collect(Collectors.toList());

        Assertions.assertEquals(1, replacements.size());
        Assertions.assertEquals(date, replacements.get(0).getText());
        Assertions.assertEquals(expected, replacements.get(0).getSuggestions().get(0).getText());
        Assertions.assertEquals(subtype, replacements.get(0).getSubtype());
    }

    @ParameterizedTest
    @ValueSource(strings = { "Desde agosto del 2019", "desde setiembre de 2020", "de agosto de 2000" })
    void testValidMonthYear(String date) {
        List<Replacement> replacements = monthYearFinders
            .stream()
            .flatMap(finder -> finder.findStream(date, WikipediaLanguage.SPANISH))
            .collect(Collectors.toList());

        Assertions.assertTrue(replacements.isEmpty());
    }
}
