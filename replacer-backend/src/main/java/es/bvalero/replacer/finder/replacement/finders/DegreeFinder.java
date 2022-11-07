package es.bvalero.replacer.finder.replacement.finders;

import static es.bvalero.replacer.finder.util.FinderUtils.DECIMAL_SEPARATORS;
import static es.bvalero.replacer.finder.util.FinderUtils.NON_BREAKING_SPACE;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import es.bvalero.replacer.common.domain.Replacement;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.Suggestion;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.replacement.ReplacementFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find temperature degrees with the wrong symbol
 */
@Component
public class DegreeFinder implements ReplacementFinder {

    private static final char DEGREE = '\u00b0'; // °
    private static final char MASCULINE_ORDINAL = '\u00ba'; // º
    private static final String CELSIUS = "C";
    private static final String FAHRENHEIT = "F";
    private static final String KELVIN = "K";
    private static final Set<String> DEGREE_LETTERS = Set.of(CELSIUS, FAHRENHEIT, KELVIN);
    private static final char CELSIUS_UNICODE = '\u2103'; // ℃
    private static final char FAHRENHEIT_UNICODE = '\u2109'; // ℉

    @Override
    public Iterable<MatchResult> findMatchResults(WikipediaPage page) {
        // The performance is about 5x better than an automaton approach
        return LinearMatchFinder.find(page, this::findDegree);
    }

    @Nullable
    private MatchResult findDegree(WikipediaPage page, int start) {
        final String text = page.getContent();
        while (start >= 0 && start < text.length()) {
            final LinearMatchResult matchSymbol = findDegreeSymbol(text, start);
            if (matchSymbol == null) {
                break;
            }
            final int startSymbol = matchSymbol.start();

            // They can be null if the symbol is a Unicode character
            String space2 = null;
            LinearMatchResult matchLetter = null;
            if (!isUnicodeSymbol(matchSymbol.group())) {
                matchLetter = findDegreeLetter(text, matchSymbol.end());
                if (matchLetter == null) {
                    start = startSymbol + 1;
                    continue;
                }

                space2 = text.substring(matchSymbol.end(), matchLetter.start());
                if (StringUtils.isNotBlank(space2)) {
                    start = startSymbol + 1;
                    continue;
                }
            }

            final LinearMatchResult matchBefore = FinderUtils.findWordBefore(text, startSymbol, DECIMAL_SEPARATORS);
            if (matchBefore == null) {
                // This would only happen if the degree is at the very start of the content, but we need to check it,
                start = startSymbol + 1;
                continue;
            }

            final int endDegree = matchLetter == null ? matchSymbol.end() : matchLetter.end();
            final String word = matchBefore.group();
            final String space1 = text.substring(matchBefore.end(), startSymbol);
            assert matchSymbol.group().length() == 1;
            final char symbol = matchSymbol.group().charAt(0);
            final String letter = matchLetter == null ? null : matchLetter.group();
            if (isValidDegree(word, space1, symbol, space2, letter)) {
                start = endDegree;
                continue;
            }
            // If preceded by number the space must be valid
            if (StringUtils.isNumeric(word) && !FinderUtils.isSpace(space1)) {
                start = endDegree;
                continue;
            }

            final LinearMatchResult match = LinearMatchResult.of(
                matchBefore.start(),
                text.substring(matchBefore.start(), endDegree)
            );
            match.addGroup(matchBefore);
            match.addGroup(LinearMatchResult.of(matchBefore.end(), space1));
            match.addGroup(matchLetter == null ? matchSymbol : matchLetter);
            return match;
        }
        return null;
    }

    @Nullable
    private LinearMatchResult findDegreeSymbol(String text, int start) {
        final String textSearchable = text.substring(start);
        int startSymbol = StringUtils.indexOfAny(
            textSearchable,
            DEGREE,
            MASCULINE_ORDINAL,
            CELSIUS_UNICODE,
            FAHRENHEIT_UNICODE
        );
        return startSymbol < 0
            ? null
            : LinearMatchResult.of(start + startSymbol, String.valueOf(textSearchable.charAt(startSymbol)));
    }

    private boolean isUnicodeSymbol(String symbol) {
        assert symbol.length() == 1;
        final char symbolChar = symbol.charAt(0);
        return symbolChar == CELSIUS_UNICODE || symbolChar == FAHRENHEIT_UNICODE;
    }

    @Nullable
    private LinearMatchResult findDegreeLetter(String text, int startSymbol) {
        final LinearMatchResult matchAfter = FinderUtils.findWordAfter(text, startSymbol);
        if (
            matchAfter == null ||
            matchAfter.group().length() != 1 ||
            !DEGREE_LETTERS.contains(FinderUtils.toUpperCase(matchAfter.group()))
        ) {
            return null;
        } else {
            return matchAfter;
        }
    }

    // A degree is valid if it contains a space and the symbol is correct
    private boolean isValidDegree(
        String word,
        String space1,
        char symbol,
        @Nullable String space2,
        @Nullable String letter
    ) {
        if (space2 == null || letter == null) {
            // Unicode character
            return false;
        }

        // Only check previous space if the word is a number
        if (FinderUtils.isDecimalNumber(word)) {
            if (letter.equalsIgnoreCase(KELVIN)) {
                return false;
            } else {
                return (
                    FinderUtils.isActualSpace(space1) &&
                    symbol == DEGREE &&
                    EMPTY.equals(space2) &&
                    DEGREE_LETTERS.contains(letter)
                );
            }
        } else {
            return symbol == DEGREE && EMPTY.equals(space2) && DEGREE_LETTERS.contains(letter);
        }
    }

    @Override
    public Replacement convert(MatchResult matchResult, WikipediaPage page) {
        final LinearMatchResult match = (LinearMatchResult) matchResult;

        final String fixedDegree;
        final String word = match.group(0);
        final String space1 = match.group(1);
        final String fixedLetter;
        final String fixedSymbol;
        if (isUnicodeSymbol(match.group(2))) {
            fixedLetter = match.group(2).charAt(0) == FAHRENHEIT_UNICODE ? FAHRENHEIT : CELSIUS;
            fixedSymbol = String.valueOf(DEGREE);
        } else {
            fixedLetter = FinderUtils.toUpperCase(match.group(2));
            fixedSymbol = fixedLetter.equals(KELVIN) ? "" : String.valueOf(DEGREE);
        }

        final int start;
        final String text;
        if (FinderUtils.isDecimalNumber(word)) {
            final String fixedSpace = FinderUtils.isNonBreakingSpace(space1) ? space1 : NON_BREAKING_SPACE;
            fixedDegree = word + fixedSpace + fixedSymbol + fixedLetter;
            start = match.start();
            text = match.group();
        } else {
            fixedDegree = fixedSymbol + fixedLetter;
            final int offset = word.length() + space1.length();
            start = match.start() + offset;
            text = match.group().substring(offset);
        }

        return Replacement
            .builder()
            .type(ReplacementType.DEGREES)
            .start(start)
            .text(text)
            .suggestions(List.of(Suggestion.ofNoComment(fixedDegree)))
            .build();
    }
}
