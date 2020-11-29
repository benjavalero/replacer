package es.bvalero.replacer.finder.date;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.*;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.intellij.lang.annotations.RegExp;
import org.springframework.stereotype.Component;

/**
 * Find dates to be corrected, e.g. with the month in uppercase
 */
@Component
public class DateFinder implements ReplacementFinder {
    private static final String TYPE_DATE = "Fechas";
    static final String SUBTYPE_DOT_YEAR = "Año con punto";
    static final String SUBTYPE_INCOMPLETE = "Fecha incompleta";
    static final String SUBTYPE_LEADING_ZERO = "Día con cero";
    static final String SUBTYPE_UPPERCASE = "Mes en mayúscula";

    private static final List<String> MONTHS_LOWERCASE = Arrays.asList(
        "enero",
        "febrero",
        "marzo",
        "abril",
        "mayo",
        "junio",
        "julio",
        "agosto",
        "sep?tiembre",
        "octubre",
        "noviembre",
        "diciembre"
    );

    private static final List<String> MONTHS_UPPERCASE_CLASS = MONTHS_LOWERCASE
        .stream()
        .map(FinderUtils::setFirstUpperCaseClass)
        .collect(Collectors.toList());

    private static final List<String> CONNECTORS = Arrays.asList(
        "a",
        "desde",
        "de",
        "durante",
        "el",
        "entre",
        "en",
        "hacia",
        "hasta",
        "para",
        "y"
    );

    private static final List<String> CONNECTORS_UPPERCASE_CLASS = CONNECTORS
        .stream()
        .map(FinderUtils::setFirstUpperCaseClass)
        .collect(Collectors.toList());

    @RegExp
    private static final String REGEX_DATE = "(%s|(3[01]|[012]?<N>)( [Dd]e)?) (%s) ([Dd]el? )?[12]\\.?<N>{3}";

    private static final RunAutomaton AUTOMATON_UPPERCASE_LONG_DATE = new RunAutomaton(
        new dk.brics.automaton.RegExp(
            String.format(
                REGEX_DATE,
                StringUtils.join(CONNECTORS_UPPERCASE_CLASS, "|"),
                StringUtils.join(MONTHS_UPPERCASE_CLASS, "|")
            )
        )
        .toAutomaton(new DatatypesAutomatonProvider())
    );

    @Override
    public Iterable<Replacement> find(String text, WikipediaLanguage lang) {
        if (WikipediaLanguage.SPANISH == lang) {
            return new RegexIterable<>(text, AUTOMATON_UPPERCASE_LONG_DATE, this::convert, this::isValidMatch);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean isValidMatch(MatchResult match, String text) {
        return ReplacementFinder.super.isValidMatch(match, text) && !isValidDate(match.group());
    }

    private boolean isValidDate(String date) {
        return startsWithNumber(date) ? isValidLongDate(date) : isValidMonthYear(date);
    }

    private boolean isValidLongDate(String date) {
        List<String> tokens = new LinkedList<>(Arrays.asList(date.split(" ")));
        return (
            tokens.size() == 5 &&
            FinderUtils.toLowerCase(date).equals(date) &&
            !date.startsWith("0") &&
            tokens.get(4).length() == 4
        );
    }

    private boolean isValidMonthYear(String date) {
        List<String> tokens = new LinkedList<>(Arrays.asList(date.split(" ")));
        return (
            tokens.size() == 4 &&
            FinderUtils.toLowerCase(date.substring(1)).equals(date.substring(1)) &&
            tokens.get(3).length() == 4
        );
    }

    private Replacement convert(MatchResult matcher) {
        return startsWithNumber(matcher.group()) ? convertLongDate(matcher) : convertMonthYear(matcher);
    }

    private boolean startsWithNumber(String text) {
        return Character.isDigit(text.charAt(0));
    }

    private Replacement convertLongDate(MatchResult match) {
        String date = match.group();
        List<String> tokens = new LinkedList<>(Arrays.asList(date.split(" ")));
        String subtype = null;

        // Fix year with dot
        String year = tokens.get(tokens.size() - 1);
        String fixedYear = fixYearWithDot(year);
        if (!fixedYear.equals(year)) {
            subtype = SUBTYPE_DOT_YEAR;
            tokens.set(tokens.size() - 1, fixedYear);
        }

        // Add missing prepositions
        if (isNotPreposition(tokens.get(1))) {
            tokens.add(1, "de");
            subtype = SUBTYPE_INCOMPLETE;
        }
        if (isNotPreposition(tokens.get(3))) {
            tokens.add(3, "de");
            subtype = SUBTYPE_INCOMPLETE;
        }

        // Fix leading zero
        String day = tokens.get(0);
        String fixedDay = fixLeadingZero(day);
        if (!fixedDay.equals(day)) {
            subtype = SUBTYPE_LEADING_ZERO;
            tokens.set(0, fixedDay);
        }

        // Fix uppercase
        String fixedDate = StringUtils.join(tokens, " ");
        String lowerDate = FinderUtils.toLowerCase(fixedDate);
        if (!lowerDate.equals(fixedDate)) {
            subtype = SUBTYPE_UPPERCASE;
            fixedDate = lowerDate;
        }

        // Fix September
        fixedDate = fixSeptember(fixedDate);

        if (subtype == null) {
            throw new IllegalArgumentException(String.format("Not valid date to convert: %s", date));
        }

        return Replacement
            .builder()
            .type(TYPE_DATE)
            .subtype(subtype)
            .start(match.start())
            .text(date)
            .suggestions(findSuggestions(fixedDate))
            .build();
    }

    private Replacement convertMonthYear(MatchResult match) {
        String date = match.group();
        List<String> tokens = new LinkedList<>(Arrays.asList(date.split(" ")));
        String subtype = null;

        // Fix year with dot
        String year = tokens.get(tokens.size() - 1);
        String fixedYear = fixYearWithDot(year);
        if (!fixedYear.equals(year)) {
            subtype = SUBTYPE_DOT_YEAR;
            tokens.set(tokens.size() - 1, fixedYear);
        }

        // Add missing prepositions
        if (isNotPreposition(tokens.get(2))) {
            tokens.add(2, "de");
            subtype = SUBTYPE_INCOMPLETE;
        }

        // Fix uppercase
        String monthYear = StringUtils.join(tokens.subList(1, tokens.size()), " ");
        String lowerMonthYear = FinderUtils.toLowerCase(monthYear);
        if (!lowerMonthYear.equals(monthYear)) {
            subtype = SUBTYPE_UPPERCASE;
            monthYear = lowerMonthYear;
        }

        // Fix September
        monthYear = fixSeptember(monthYear);

        String fixedDate = String.format("%s %s", tokens.get(0), monthYear);

        if (subtype == null) {
            throw new IllegalArgumentException(String.format("Not valid date to convert: %s", date));
        }

        return Replacement
            .builder()
            .type(TYPE_DATE)
            .subtype(subtype)
            .start(match.start())
            .text(date)
            .suggestions(findSuggestions(fixedDate))
            .build();
    }

    private String fixYearWithDot(String year) {
        return year.charAt(1) == '.' ? year.charAt(0) + year.substring(2) : year;
    }

    private boolean isNotPreposition(String word) {
        return !FinderUtils.toLowerCase(word).startsWith("de");
    }

    private String fixLeadingZero(String day) {
        return day.startsWith("0") ? day.substring(1) : day;
    }

    private String fixSeptember(String date) {
        return date.replace("setiembre", "septiembre");
    }

    private List<Suggestion> findSuggestions(String date) {
        return Collections.singletonList(Suggestion.ofNoComment(date));
    }
}
