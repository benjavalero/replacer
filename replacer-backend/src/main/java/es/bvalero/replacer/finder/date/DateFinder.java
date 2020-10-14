package es.bvalero.replacer.finder.date;

import es.bvalero.replacer.finder.FinderUtils;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.Suggestion;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;

/**
 * Abstract class with the common functionality to find replacements of type Date Format
 */
abstract class DateFinder {
    private static final String TYPE_DATE = "Fechas";
    private static final String SUBTYPE_DATE_UPPERCASE_MONTHS = "Mes en mayúscula";
    private static final String SUBTYPE_DATE_LEADING_ZERO = "Día con cero";
    private static final String SUBTYPE_DATE_YEAR_WITH_ARTICLE = "Año con artículo";
    private static final String SUBTYPE_DATE_INCOMPLETE = "Fecha incompleta";
    static final List<String> MONTHS = Arrays.asList(
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
    static final List<String> MONTHS_UPPERCASE_CLASS = MONTHS
        .stream()
        .map(FinderUtils::setFirstUpperCaseClass)
        .collect(Collectors.toList());

    Replacement convertMatch(MatchResult matcher) {
        int start = matcher.start();
        String text = matcher.group();
        return Replacement
            .builder()
            .type(TYPE_DATE)
            .subtype(getSubtype(text))
            .start(start)
            .text(text)
            .suggestions(findSuggestions(matcher))
            .build();
    }

    public String getSubtype(String text) {
        if (FinderUtils.containsUppercase(text)) {
            return SUBTYPE_DATE_UPPERCASE_MONTHS;
        } else if (text.startsWith("0")) {
            return SUBTYPE_DATE_LEADING_ZERO;
        } else if (text.toLowerCase().contains("del")) {
            return SUBTYPE_DATE_YEAR_WITH_ARTICLE;
        } else {
            return SUBTYPE_DATE_INCOMPLETE;
        }
    }

    private List<Suggestion> findSuggestions(MatchResult matcher) {
        return Collections.singletonList(Suggestion.ofNoComment(fixDate(matcher)));
    }

    abstract String fixDate(MatchResult matcher);

    String fixMonth(String month) {
        String lower = FinderUtils.toLowerCase(month);
        return lower.replace("setiembre", "septiembre");
    }
}
