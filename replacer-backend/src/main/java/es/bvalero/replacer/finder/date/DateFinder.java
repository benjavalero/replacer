package es.bvalero.replacer.finder.date;

import es.bvalero.replacer.finder.FinderUtils;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.Suggestion;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/**
 * Abstract class with the common functionality to find replacements of type Date
 */
abstract class DateFinder {
    static final List<String> MONTHS_LOWERCASE = Arrays.asList(
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
    static final List<String> MONTHS_UPPERCASE = MONTHS_LOWERCASE
        .stream()
        .map(FinderUtils::setFirstUpperCase)
        .collect(Collectors.toList());

    private static final String TYPE_DATE = "Fechas";

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
    static final List<String> CONNECTORS_UPPERCASE_CLASS = CONNECTORS
        .stream()
        .map(FinderUtils::setFirstUpperCaseClass)
        .collect(Collectors.toList());

    Replacement convertLongDate(MatchResult matcher) {
        int start = matcher.start();
        String text = matcher.group();
        return Replacement
            .builder()
            .type(TYPE_DATE)
            .subtype(getSubtype())
            .start(start)
            .text(text)
            .suggestions(findLongDateSuggestions(text))
            .build();
    }

    Replacement convertMonthYear(MatchResult matcher) {
        int start = matcher.start();
        String text = matcher.group();
        return Replacement
            .builder()
            .type(TYPE_DATE)
            .subtype(getSubtype())
            .start(start)
            .text(text)
            .suggestions(findMonthYearSuggestions(text))
            .build();
    }

    abstract String getSubtype();

    private List<Suggestion> findLongDateSuggestions(String date) {
        return Collections.singletonList(Suggestion.ofNoComment(fixLongDate(date)));
    }

    private List<Suggestion> findMonthYearSuggestions(String date) {
        return Collections.singletonList(Suggestion.ofNoComment(fixMonthYear(date)));
    }

    private String fixLongDate(String date) {
        String fixedDate = fixUppercaseLongDate(date);
        fixedDate = fixSeptember(fixedDate);
        fixedDate = fixLeadingZero(fixedDate);
        fixedDate = fixYearWithDot(fixedDate);
        fixedDate = fixYearWithArticle(fixedDate);
        fixedDate = fixIncompleteLongDate(fixedDate);
        return fixedDate;
    }

    private String fixUppercaseLongDate(String date) {
        return FinderUtils.toLowerCase(date);
    }

    private String fixMonthYear(String date) {
        String fixedDate = fixUppercaseMonthYear(date);
        fixedDate = fixSeptember(fixedDate);
        fixedDate = fixYearWithDot(fixedDate);
        fixedDate = fixYearWithArticle(fixedDate);
        fixedDate = fixIncompleteMonthYear(fixedDate);
        return fixedDate;
    }

    private String fixUppercaseMonthYear(String date) {
        int posSpace = date.indexOf(' ');
        return date.substring(0, posSpace) + FinderUtils.toLowerCase(date.substring(posSpace));
    }

    private String fixSeptember(String date) {
        return date.replace("setiembre", "septiembre");
    }

    private String fixLeadingZero(String date) {
        return date.startsWith("0") ? date.substring(1) : date;
    }

    private String fixYearWithDot(String date) {
        return date.charAt(date.length() - 4) == '.'
            ? date.substring(0, date.length() - 4) + date.substring(date.length() - 3)
            : date;
    }

    private String fixYearWithArticle(String date) {
        return date.replace("del", "de");
    }

    private String fixIncompleteLongDate(String date) {
        List<String> tokens = new LinkedList<>(Arrays.asList(date.split(" ")));
        if (!tokens.get(1).equals("de")) {
            tokens.add(1, "de");
        }
        if (!tokens.get(3).equals("de")) {
            tokens.add(3, "de");
        }
        return StringUtils.join(tokens, " ");
    }

    private String fixIncompleteMonthYear(String date) {
        List<String> tokens = new LinkedList<>(Arrays.asList(date.split(" ")));
        if (!tokens.get(2).equals("de")) {
            tokens.add(2, "de");
        }
        return StringUtils.join(tokens, " ");
    }
}
