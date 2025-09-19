package es.bvalero.replacer.finder.replacement.finders;

import static es.bvalero.replacer.finder.util.FinderUtils.*;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.StandardType;
import es.bvalero.replacer.finder.Suggestion;
import es.bvalero.replacer.finder.replacement.ReplacementFinder;
import es.bvalero.replacer.finder.util.FinderMatchResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find temperature degrees with the wrong symbol
 */
@Component
class DegreeFinder implements ReplacementFinder {

    private static final char CELSIUS = 'C';
    private static final char FAHRENHEIT = 'F';
    private static final char KELVIN = 'K';
    private static final char CELSIUS_UNICODE = '\u2103'; // ℃
    private static final char FAHRENHEIT_UNICODE = '\u2109'; // ℉
    private static final char SPACE = ' ';
    private static final char[] DECIMAL_SEPARATORS = new char[] { DOT, DECIMAL_COMMA };

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        // The performance is about 5x better than an automaton approach
        // Using an automaton or Aho-Corasick to capture the degree symbols is 3x slower
        return LinearMatchFinder.find(page, this::findDegree);
    }

    @SuppressWarnings("ArgumentSelectionDefectChecker")
    @Nullable
    private MatchResult findDegree(FinderPage page, int start) {
        final String text = page.getContent();
        while (start >= 0 && start < text.length()) {
            // Find the degree symbol, e.g. ºC
            final MatchResult matchSymbol = findDegreeSymbol(text, start);
            if (matchSymbol == null) {
                return null;
            }
            final int startSymbol = matchSymbol.start();
            final int endDegree = matchSymbol.end();

            // Find the word before (not necessarily a number)
            final MatchResult matchWord = findDegreeWord(text, startSymbol);
            if (matchWord == null) {
                start = endDegree;
                continue;
            }

            final int startDegree = matchWord.start();
            final FinderMatchResult match = FinderMatchResult.of(text, startDegree, endDegree);
            // 1 - word; 2 - space before; 3 - symbol
            match.addGroup(matchWord);
            match.addGroup(FinderMatchResult.of(text, matchWord.end(), startSymbol));
            match.addGroup(matchSymbol);
            return match;
        }
        return null;
    }

    @Nullable
    private MatchResult findDegreeSymbol(String text, int start) {
        while (start >= 0 && start < text.length()) {
            // First we find the initial symbol
            final int startSymbol = FinderUtils.indexOfAny(
                text,
                start,
                DEGREE,
                MASCULINE_ORDINAL,
                CELSIUS_UNICODE,
                FAHRENHEIT_UNICODE
            );
            if (startSymbol < 0) {
                return null;
            }

            // If it is a Unicode symbol we are done
            final char degreeSymbol = text.charAt(startSymbol);
            if (isUnicodeSymbol(degreeSymbol)) {
                return FinderMatchResult.of(startSymbol, Character.toString(degreeSymbol));
            }

            // Find the degree letter. We admit a whitespace between.
            if (startSymbol + 1 >= text.length()) {
                return null;
            }
            final int startLetter = text.charAt(startSymbol + 1) == SPACE ? startSymbol + 2 : startSymbol + 1;
            if (
                startLetter >= text.length() ||
                !isDegreeLetter(text.charAt(startLetter)) ||
                !FinderUtils.isWordCompleteInText(startLetter, startLetter + 1, text)
            ) {
                // Keep on searching
                start = startSymbol + 1;
                continue;
            }

            return FinderMatchResult.of(text, startSymbol, startLetter + 1);
        }
        return null;
    }

    private boolean isUnicodeSymbol(char symbolChar) {
        return symbolChar == CELSIUS_UNICODE || symbolChar == FAHRENHEIT_UNICODE;
    }

    private boolean isDegreeLetter(char ch) {
        return ch == CELSIUS || ch == FAHRENHEIT || ch == KELVIN;
    }

    @Nullable
    private MatchResult findDegreeWord(String text, int startSymbol) {
        final MatchResult matchBefore = FinderUtils.findWordBefore(text, startSymbol, false, DECIMAL_SEPARATORS);
        if (matchBefore == null) {
            return null;
        }
        // If preceded by number, there must be a space (or nothing) between.
        if (
            FinderUtils.isDecimalNumber(matchBefore.group()) &&
            !FinderUtils.isBlankOrNonBreakingSpace(text, matchBefore.end(), startSymbol)
        ) {
            return null;
        }
        return matchBefore;
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        final String symbol = match.group(3);
        assert symbol.length() <= 3;
        // Check the symbol
        // Contains whitespace / Unicode symbol / Kelvin degree / Ordinal symbol
        if (
            symbol.length() == 3 ||
            symbol.length() == 1 ||
            symbol.charAt(symbol.length() - 1) == KELVIN ||
            symbol.charAt(0) != DEGREE
        ) {
            return true;
        }

        // If the symbol doesn't need to be fixed, we check the space before.
        if (!FinderUtils.isBlankOrNonBreakingSpace(page.getContent(), match.start(2), match.end(2))) {
            return false;
        }

        // Between number and degree, we don't fix actual spaces.
        // We know that the space is not a non-breaking space.
        return FinderUtils.isDecimalNumber(match.group(1)) && !FinderUtils.isActualSpace(match.group(2));
    }

    // TODO: Implement conversion without suggestions

    @Override
    public Replacement convert(MatchResult match, FinderPage page) {
        // 1 - word; 2 - space before; 3 - symbol
        final String word = match.group(1);
        final String space1 = match.group(2);
        final String symbol = match.group(3);

        final char fixedLetter;
        final String fixedSymbol;
        String suggestion = null;
        if (isUnicodeSymbol(symbol.charAt(0))) {
            fixedLetter = symbol.charAt(0) == FAHRENHEIT_UNICODE ? FAHRENHEIT : CELSIUS;
            fixedSymbol = String.valueOf(DEGREE);
            suggestion = "carácter Unicode";
        } else {
            fixedLetter = symbol.charAt(symbol.length() - 1);
            if (fixedLetter == KELVIN) {
                fixedSymbol = EMPTY;
            } else {
                fixedSymbol = String.valueOf(DEGREE);
                if (symbol.charAt(0) == MASCULINE_ORDINAL) {
                    suggestion = "símbolo de ordinal";
                }
            }
        }

        final String fixedDegree;
        final int start;
        final String text;
        if (FinderUtils.isDecimalNumber(word)) {
            final boolean isNonBreakingSpace = FinderUtils.isNonBreakingSpace(
                page.getContent(),
                match.start(2),
                match.end(2)
            );
            final String fixedSpace = isNonBreakingSpace ? space1 : NON_BREAKING_SPACE;
            fixedDegree = word + fixedSpace + fixedSymbol + fixedLetter;
            start = match.start();
            text = match.group();
        } else {
            fixedDegree = fixedSymbol + fixedLetter;
            final int offset = word.length() + space1.length();
            start = match.start() + offset;
            text = match.group().substring(offset);
        }

        final List<Suggestion> suggestions = new java.util.ArrayList<>();
        suggestions.add(Suggestion.of(text, suggestion));
        suggestions.add(Suggestion.of(fixedDegree, "grados"));

        // Exception: sometimes 1ºC might be an ordinal, e.g. the group of a sports competition.
        if (
            isNumeric(word) &&
            StringUtils.isEmpty(space1) &&
            symbol.charAt(0) == MASCULINE_ORDINAL &&
            isDegreeLetter(symbol.charAt(1))
        ) {
            suggestions.add(Suggestion.of(word + DOT + symbol, "ordinal"));
        }

        return Replacement.of(start, text, StandardType.DEGREES, suggestions);
    }
}
