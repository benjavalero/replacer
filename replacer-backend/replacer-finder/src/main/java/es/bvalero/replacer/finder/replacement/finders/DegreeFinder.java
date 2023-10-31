package es.bvalero.replacer.finder.replacement.finders;

import static es.bvalero.replacer.finder.util.FinderUtils.*;

import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.Suggestion;
import es.bvalero.replacer.finder.replacement.ReplacementFinder;
import es.bvalero.replacer.finder.util.FinderMatchResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
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
class DegreeFinder implements ReplacementFinder {

    private static final char CELSIUS = 'C';
    private static final char FAHRENHEIT = 'F';
    private static final char KELVIN = 'K';
    private static final Set<Character> DEGREE_LETTERS = Set.of(CELSIUS, FAHRENHEIT, KELVIN);
    private static final char CELSIUS_UNICODE = '\u2103'; // ℃
    private static final char FAHRENHEIT_UNICODE = '\u2109'; // ℉
    private static final char SPACE = ' ';

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
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
        // First we find the initial symbol
        final String textSearchable = text.substring(start);
        int startSymbol = StringUtils.indexOfAny(
            textSearchable,
            DEGREE,
            MASCULINE_ORDINAL,
            CELSIUS_UNICODE,
            FAHRENHEIT_UNICODE
        );
        if (startSymbol < 0) {
            return null;
        } else {
            startSymbol = start + startSymbol;
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
        if (startLetter >= text.length() || !DEGREE_LETTERS.contains(text.charAt(startLetter))) {
            return null;
        }

        return FinderMatchResult.of(text, startSymbol, startLetter + 1);
    }

    private boolean isUnicodeSymbol(char symbolChar) {
        return symbolChar == CELSIUS_UNICODE || symbolChar == FAHRENHEIT_UNICODE;
    }

    @Nullable
    private MatchResult findDegreeWord(String text, int startSymbol) {
        final MatchResult matchBefore = FinderUtils.findWordBefore(text, startSymbol, DECIMAL_SEPARATORS, false);
        if (matchBefore == null) {
            return null;
        }
        // If preceded by number, there must be a space (or nothing) between.
        final String word = matchBefore.group();
        final String space1 = text.substring(matchBefore.end(), startSymbol);
        if (FinderUtils.isDecimalNumber(word) && !FinderUtils.isBlankOrNonBreakingSpace(space1)) {
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

        // If the symbol doesn't need to be fixed, we check the space before if preceded by number.
        // Between number and degree, we don't fix actual spaces.
        final String word = match.group(1);
        final String space1 = match.group(2);
        return FinderUtils.isDecimalNumber(word) && !FinderUtils.isActualSpace(space1);
    }

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
                fixedSymbol = "";
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

        final List<Suggestion> suggestions = List.of(
            Suggestion.of(text, suggestion),
            Suggestion.ofNoComment(fixedDegree)
        );

        return Replacement
            .builder()
            .page(page)
            .type(StandardType.DEGREES)
            .start(start)
            .text(text)
            .suggestions(suggestions)
            .build();
    }
}
