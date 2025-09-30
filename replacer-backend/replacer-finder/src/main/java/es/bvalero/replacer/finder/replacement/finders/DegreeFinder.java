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
    private static final char[] DECIMAL_SEPARATORS = new char[] { DOT, DECIMAL_COMMA };

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        // The performance is about 5x better than an automaton approach
        // Using an automaton or Aho-Corasick to capture the degree symbols is 3x slower
        return LinearMatchFinder.find(page, this::findDegree);
    }

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
            final int endSymbol = matchSymbol.end();

            // Find the word before (not necessarily a number)
            final MatchResult matchWord = findDegreeWord(text, startSymbol);
            if (matchWord == null) {
                start = endSymbol;
                continue;
            }

            final int startDegree = FinderUtils.isDecimalNumber(matchWord.group()) ? matchWord.start() : startSymbol;
            final FinderMatchResult match = FinderMatchResult.ofNested(text, startDegree, endSymbol);
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
                MASCULINE_ORDINAL,
                DEGREE,
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

            // Find the degree letter
            int endSymbol = startSymbol + 1;
            if (endSymbol >= text.length()) {
                return null;
            }
            char symbolLetter = text.charAt(endSymbol++);
            // We admit a whitespace between
            if (FinderUtils.isWhiteSpace(symbolLetter)) {
                if (endSymbol >= text.length()) {
                    return null;
                }
                symbolLetter = text.charAt(endSymbol++);
            }
            if (isDegreeLetter(symbolLetter) && FinderUtils.isWordCompleteInText(startSymbol, endSymbol, text)) {
                return FinderMatchResult.of(text, startSymbol, endSymbol);
            }

            // Keep on searching
            start = endSymbol + 1;
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
        final MatchResult matchBefore = FinderUtils.findWordBefore(text, startSymbol, DECIMAL_SEPARATORS);
        if (matchBefore == null) {
            return null;
        }
        // If preceded by number, there must be a space (or nothing) between.
        if (
            FinderUtils.isDecimalNumber(matchBefore.group()) &&
            !FinderUtils.isEmptyBlankOrSpaceAlias(text, matchBefore.end(), startSymbol)
        ) {
            return null;
        }
        return matchBefore;
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        final String text = page.getContent();
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

        // Between number and degree, we don't fix actual spaces.
        // We know that the space is not a non-breaking space.
        return (
            FinderUtils.isDecimalNumber(match.group(1)) &&
            !FinderUtils.isWhiteSpaceOrAlias(text, match.start(2), match.end(2))
        );
    }

    @Override
    public Replacement convertWithNoSuggestions(MatchResult match, FinderPage page) {
        return Replacement.ofNoSuggestions(match.start(), match.group(), StandardType.DEGREES);
    }

    @Override
    public Replacement convert(MatchResult match, FinderPage page) {
        // 1 - word; 2 - space before; 3 - symbol
        final String word = match.group(1);
        final boolean isNumericWord = FinderUtils.isDecimalNumber(word);
        final String space1 = match.group(2);
        final String symbol = match.group(3);

        String suggestion = null;

        // Fix space between word and symbol (if needed)
        String fixedSpace1 = space1;
        if (isNumericWord) {
            final boolean isHardSpaceAlias = FinderUtils.isHardSpaceAlias(
                page.getContent(),
                match.start(2),
                match.end(2)
            );
            if (!isHardSpaceAlias) {
                fixedSpace1 = NON_BREAKING_SPACE;
            }
        }

        // Fix symbol
        final char startSymbol = symbol.charAt(0);
        String fixedSymbol = String.valueOf(startSymbol);
        char fixedLetter = symbol.charAt(symbol.length() - 1);
        if (isUnicodeSymbol(symbol.charAt(0))) {
            fixedSymbol = String.valueOf(DEGREE);
            fixedLetter = symbol.charAt(0) == FAHRENHEIT_UNICODE ? FAHRENHEIT : CELSIUS;
            suggestion = "carácter Unicode";
        } else if (fixedLetter == KELVIN) {
            fixedSymbol = EMPTY;
        } else if (startSymbol == MASCULINE_ORDINAL) {
            fixedSymbol = String.valueOf(DEGREE);
            suggestion = "símbolo de ordinal";
        }

        final String fixedDegree = isNumericWord
            ? word + fixedSpace1 + fixedSymbol + fixedLetter
            : fixedSymbol + fixedLetter;

        final List<Suggestion> suggestions = new java.util.ArrayList<>();
        suggestions.add(Suggestion.of(match.group(), suggestion));
        suggestions.add(Suggestion.of(fixedDegree, "grados"));

        // Exception: sometimes 1ºC might be an ordinal, e.g. the group of a sports competition.
        if (
            isNumber(word) &&
            StringUtils.isEmpty(space1) &&
            startSymbol == MASCULINE_ORDINAL &&
            isDegreeLetter(symbol.charAt(1))
        ) {
            suggestions.add(Suggestion.of(word + DOT + symbol, "ordinal"));
        }

        return Replacement.of(match.start(), match.group(), StandardType.DEGREES, suggestions);
    }
}
