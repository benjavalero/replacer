package es.bvalero.replacer.finder.date;

import es.bvalero.replacer.finder.FinderUtils;
import es.bvalero.replacer.finder.RegexIterable;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFinder;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Find non-valid dates without day, e.g. `Desde Octubre 2019`
 */
@Component
class MonthYearFinder extends DateFinder implements ReplacementFinder {
    private static final List<String> WORDS = Arrays.asList(
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
    private static final List<String> WORDS_UPPERCASE_CLASS = WORDS
        .stream()
        .map(FinderUtils::setFirstUpperCaseClass)
        .collect(Collectors.toList());

    private static final String REGEX_MONTH_YEAR = "(%s) (%s) ([Dd]el? )?(\\d{4})";
    private static final String REGEX_MONTH_YEAR_VALID = "(%s) (%s) de \\d{4}";
    private static final Pattern PATTERN_MONTH_YEAR = Pattern.compile(
        String.format(
            REGEX_MONTH_YEAR,
            StringUtils.join(WORDS_UPPERCASE_CLASS, "|"),
            StringUtils.join(MONTHS_UPPERCASE_CLASS, "|")
        )
    );
    private static final Pattern PATTERN_MONTH_YEAR_VALID = Pattern.compile(
        String.format(
            REGEX_MONTH_YEAR_VALID,
            StringUtils.join(WORDS_UPPERCASE_CLASS, "|"),
            StringUtils.join(MONTHS, "|")
        )
    );

    @Override
    public Iterable<Replacement> find(String text, WikipediaLanguage lang) {
        if (WikipediaLanguage.SPANISH == lang) {
            return new RegexIterable<>(text, PATTERN_MONTH_YEAR, this::convertMatch, this::isDateToFix);
        } else {
            return Collections.emptyList();
        }
    }

    private boolean isDateToFix(MatchResult match, String text) {
        return this.isValidMatch(match, text) && !this.isValidDate(match.group());
    }

    private boolean isValidDate(String date) {
        return PATTERN_MONTH_YEAR_VALID.matcher(date).matches();
    }

    @Override
    String fixDate(MatchResult matcher) {
        return String.format("%s %s de %s", matcher.group(1), fixMonth(matcher.group(2)), matcher.group(4));
    }
}
