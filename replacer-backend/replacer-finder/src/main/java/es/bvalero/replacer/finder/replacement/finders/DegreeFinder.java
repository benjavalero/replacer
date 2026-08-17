package es.bvalero.replacer.finder.replacement.finders;

import static es.bvalero.replacer.finder.util.FinderUtils.*;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.StandardType;
import es.bvalero.replacer.finder.Suggestion;
import es.bvalero.replacer.finder.replacement.ReplacementFinder;
import es.bvalero.replacer.finder.util.FinderMatchRange;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find temperature degrees with incorrect format, e.g.:
 * <ul>
 *   <li>{@code 50ºC} - ordinal symbol instead of degree: {@code 50&nbsp;°C}</li>
 *   <li>{@code 50°C} - missing space between number and symbol: {@code 50&nbsp;°C}</li>
 *   <li>{@code 50 ºC} - ordinal symbol: {@code 50&nbsp;°C}</li>
 *   <li>{@code 50° C} - space inside the symbol: {@code 50&nbsp;°C}</li>
 *   <li>{@code 50 ℃} - Unicode symbol: {@code 50&nbsp;°C}</li>
 *   <li>{@code 50 °K} - Kelvin (symbol not needed): {@code 50&nbsp;K}</li>
 *   <li>{@code ºC} - no number before (only the symbol is fixed): {@code °C}</li>
 * </ul>
 * Cases not corrected:
 * <ul>
 *   <li>{@code 50 °C} or {@code 50&nbsp;°C} - symbol already correct</li>
 *   <li>{@code {{unidad|5|°C}}} - non-space separator between number and symbol</li>
 *   <li>{@code (15 - 14) [°C]} - no number immediately before the symbol</li>
 * </ul>
 */
@Component
public class DegreeFinder implements ReplacementFinder {

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

            // Find the word or number immediately before the symbol (decimal separators allowed within numbers)
            final MatchResult matchWord = FinderUtils.findWordBefore(text, startSymbol, DECIMAL_SEPARATORS);
            if (matchWord == null) {
                start = endSymbol;
                continue;
            }

            // Include the number in the match only if it is a decimal number;
            // for plain words (e.g. "fusion") the match starts at the symbol.
            // If the separator is not a valid space (e.g. "400{ºC}"), skip.
            if (
                FinderUtils.isDecimalNumber(matchWord.group()) &&
                !FinderUtils.isEmptyBlankOrSpaceAlias(text, matchWord.end(), startSymbol)
            ) {
                start = endSymbol;
                continue;
            }
            final int startDegree = FinderUtils.isDecimalNumber(matchWord.group()) ? matchWord.start() : startSymbol;
            final FinderMatchRange match = FinderMatchRange.ofNested(text, startDegree, endSymbol);
            // Groups: 1 - word before; 2 - separator between word and symbol; 3 - symbol
            match.addGroup(matchWord);
            match.addGroup(FinderMatchRange.of(text, matchWord.end(), startSymbol));
            match.addGroup(matchSymbol);
            return match;
        }
        return null;
    }

    @Nullable
    private MatchResult findDegreeSymbol(String text, int start) {
        while (start >= 0 && start < text.length()) {
            // Find the next candidate: ordinal (º), degree (°), or Unicode (℃ ℉)
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

            // Unicode symbols (℃, ℉) are self-contained - no degree letter follows
            final char degreeSymbol = text.charAt(startSymbol);
            int endSymbol = startSymbol + 1;
            if (isUnicodeSymbol(degreeSymbol)) {
                // The symbol must not be immediately followed by a word character
                if (endSymbol < text.length() && FinderUtils.isWordChar(text.charAt(endSymbol))) {
                    start = endSymbol;
                    continue;
                }
                return FinderMatchRange.of(text, startSymbol, endSymbol);
            }

            // Expect a degree letter (C, F, K) after the symbol, with an optional whitespace between
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
                return FinderMatchRange.of(text, startSymbol, endSymbol);
            }

            // Keep searching
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

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        // Groups: 1 - word before; 2 - separator between word and symbol; 3 - symbol
        final String symbol = match.group(3);
        assert symbol.length() <= 3;
        // Check the symbol: inner whitespace / Unicode / Kelvin / ordinal
        if (
            symbol.length() == 3 ||
            symbol.length() == 1 ||
            symbol.charAt(symbol.length() - 1) == KELVIN ||
            symbol.charAt(0) != DEGREE
        ) {
            return true;
        }
        // Symbol is already correct (°C/°F): only fix if a decimal number is directly attached (no separator)
        return FinderUtils.isDecimalNumber(match.group(1)) && match.group(2).isEmpty();
    }

    @Override
    public Replacement convertWithNoSuggestions(MatchResult match, FinderPage page) {
        return Replacement.ofNoSuggestions(match.start(), match.group(), StandardType.DEGREES);
    }

    @Override
    public Replacement convert(MatchResult match, FinderPage page) {
        // Groups: 1 - word before; 2 - separator between word and symbol; 3 - symbol
        final String word = match.group(1);
        final boolean isNumericWord = FinderUtils.isDecimalNumber(word);
        final String space1 = match.group(2);
        final String symbol = match.group(3);

        String suggestion = null;

        // Fix the separator: replace with a non-breaking space unless it already is one
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

        // Fix the degree symbol: Unicode -> °, ordinal º -> °, Kelvin -> no symbol
        final char firstSymbolChar = symbol.charAt(0);
        String fixedSymbol = String.valueOf(firstSymbolChar);
        char fixedLetter = symbol.charAt(symbol.length() - 1);
        if (isUnicodeSymbol(symbol.charAt(0))) {
            fixedSymbol = String.valueOf(DEGREE);
            fixedLetter = symbol.charAt(0) == FAHRENHEIT_UNICODE ? FAHRENHEIT : CELSIUS;
            suggestion = "carácter Unicode";
        } else if (fixedLetter == KELVIN) {
            fixedSymbol = EMPTY;
        } else if (firstSymbolChar == MASCULINE_ORDINAL) {
            fixedSymbol = String.valueOf(DEGREE);
            suggestion = "símbolo de ordinal";
        }

        final String fixedDegree = isNumericWord
            ? word + fixedSpace1 + fixedSymbol + fixedLetter
            : fixedSymbol + fixedLetter;

        final List<Suggestion> suggestions = new java.util.ArrayList<>();
        suggestions.add(Suggestion.of(match.group(), suggestion));
        suggestions.add(Suggestion.of(fixedDegree, "grados"));

        // Special case: "1ºC" might be an ordinal (e.g. the group of a sports competition) — offer both options
        if (
            isNumber(word) &&
            StringUtils.isEmpty(space1) &&
            firstSymbolChar == MASCULINE_ORDINAL &&
            isDegreeLetter(symbol.charAt(1))
        ) {
            suggestions.add(Suggestion.of(word + DOT + symbol, "ordinal"));
        }

        return Replacement.of(match.start(), match.group(), StandardType.DEGREES, suggestions);
    }
}
