package es.bvalero.replacer.finder.date;

import es.bvalero.replacer.finder.RegexIterable;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFinder;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.util.Collections;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Find non-valid long dates, e.g. `2 de Septiembre de 2019`
 */
@Component
class LongDateFinder extends DateFinder implements ReplacementFinder {
    private static final String REGEX_LONG_DATE = "(3[01]|[012]?\\d) ([Dd]e )?(%s) ([Dd]el? )?(\\d{4})";
    private static final String REGEX_LONG_DATE_VALID = "(3[01]|[12]?\\d) de (%s) de \\d{4}";
    private static final Pattern PATTERN_LONG_DATE = Pattern.compile(
        String.format(REGEX_LONG_DATE, StringUtils.join(MONTHS_UPPERCASE_CLASS, "|"))
    );
    private static final Pattern PATTERN_LONG_DATE_VALID = Pattern.compile(
        String.format(REGEX_LONG_DATE_VALID, StringUtils.join(MONTHS, "|"))
    );

    @Override
    public Iterable<Replacement> find(String text, WikipediaLanguage lang) {
        if (WikipediaLanguage.SPANISH == lang) {
            return new RegexIterable<>(text, PATTERN_LONG_DATE, this::convertMatch, this::isDateToFix);
        } else {
            return Collections.emptyList();
        }
    }

    private boolean isDateToFix(MatchResult match, String text) {
        return this.isValidMatch(match, text) && !this.isValidDate(match.group());
    }

    private boolean isValidDate(String date) {
        return PATTERN_LONG_DATE_VALID.matcher(date).matches();
    }

    @Override
    String fixDate(MatchResult matcher) {
        return String.format(
            "%s de %s de %s",
            fixLeadingZero(matcher.group(1)),
            fixMonth(matcher.group(3)),
            matcher.group(5)
        );
    }

    private String fixLeadingZero(String day) {
        return day.startsWith("0") ? day.substring(1) : day;
    }
}
