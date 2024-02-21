package es.bvalero.replacer.finder.replacement.finders;

import static es.bvalero.replacer.finder.util.FinderUtils.*;
import static org.apache.commons.lang3.StringUtils.SPACE;

import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.Suggestion;
import es.bvalero.replacer.finder.replacement.ReplacementFinder;
import es.bvalero.replacer.finder.util.FinderMatchResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.MatchResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find coordinates with wrong degree, minute or second symbols.
 * As they are quite similar, the same finder is used to find hours with minutes and seconds.
 */
@Component
class CoordinatesFinder implements ReplacementFinder {

    private static final char HOURS = ':';
    private static final char PRIME = '\u2032'; // ′
    private static final char APOSTROPHE = '\'';
    private static final char SINGLE_QUOTE = '\u2019'; // ’
    private static final char ACUTE_ACCENT = '\u00b4'; // ´
    private static final Set<Character> PRIME_CHARS = Set.of(PRIME, APOSTROPHE, SINGLE_QUOTE, ACUTE_ACCENT);
    private static final char DOUBLE_PRIME = '\u2033'; // ″
    private static final char DOUBLE_QUOTE = '\"';
    private static final Set<Character> DOUBLE_PRIME_CHARS = Set.of(
        DOUBLE_PRIME,
        DOUBLE_QUOTE,
        START_QUOTE_TYPOGRAPHIC,
        END_QUOTE_TYPOGRAPHIC
    );
    private static final Set<String> CARDINAL_DIRECTIONS = Set.of(
        "N",
        "S",
        "W",
        "O",
        "E",
        "Norte",
        "Sur",
        "Este",
        "Oeste"
    );

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return LinearMatchFinder.find(page, this::findCoordinates);
    }

    @Nullable
    private MatchResult findCoordinates(FinderPage page, int start) {
        final String text = page.getContent();
        while (start >= 0 && start < text.length()) {
            boolean isValidCoordinates = true;

            // Degrees
            final MatchResult matchDegrees = findDegrees(text, start);
            if (matchDegrees == null) {
                return null;
            }
            final char degreeChar = text.charAt(matchDegrees.end());
            if (!isValidDegreeChar(degreeChar)) {
                isValidCoordinates = false;
            }

            // Minutes
            final MatchResult matchMinutes = findMinutes(text, matchDegrees.end() + 1);
            if (matchMinutes == null) {
                start = matchDegrees.end() + 1;
                continue;
            }
            final char primeChar = text.charAt(matchMinutes.end());
            if (!isValidPrimeChar(primeChar)) {
                isValidCoordinates = false;
            }

            // Seconds (optional)
            final MatchResult matchSeconds = findSeconds(text, matchMinutes.end() + 1);
            String doublePrime = null;
            int endCoordinates = matchMinutes.end() + 1;
            if (matchSeconds != null) {
                doublePrime = matchSeconds.group(1);
                endCoordinates = matchSeconds.end() + doublePrime.length();
                if (!isValidDoublePrimeChar(doublePrime)) {
                    isValidCoordinates = false;
                }
            }

            if (isValidCoordinates) {
                // Nothing to fix
                start = endCoordinates;
                continue;
            }

            // Find if there is a cardinal direction and enlarge the match
            MatchResult matchDirection = findDirection(text, endCoordinates);
            if (matchDirection != null) {
                endCoordinates = matchDirection.end();
            }

            final FinderMatchResult result = FinderMatchResult.of(text, matchDegrees.start(), endCoordinates);
            // Groups: 1 - Degrees; 2 - Minutes; 3 - Seconds (optional); 4 - Direction (optional); 5 - Degree symbol
            result.addGroup(matchDegrees);
            result.addGroup(matchMinutes);
            result.addGroup(doublePrime != null ? matchSeconds : FinderMatchResult.ofEmpty());
            result.addGroup(Objects.requireNonNullElseGet(matchDirection, FinderMatchResult::ofEmpty));
            result.addGroup(FinderMatchResult.of(matchDegrees.end(), String.valueOf(degreeChar)));
            return result;
        }
        return null;
    }

    /* Find a degrees match corresponding to a number, which must be followed by a degree symbol. */
    @Nullable
    private MatchResult findDegrees(String text, int start) {
        while (start >= 0 && start < text.length()) {
            final MatchResult matchNumber = FinderUtils.findNumber(text, start, false, true);
            if (matchNumber == null || matchNumber.end() >= text.length()) {
                // No number to continue searching
                return null;
            }
            if (!isDegreeNumber(matchNumber.group())) {
                start = matchNumber.end();
                continue;
            }

            if (!isDegreeChar(text.charAt(matchNumber.end()))) {
                start = matchNumber.end() + 1;
                continue;
            }

            return matchNumber;
        }
        return null;
    }

    private boolean isDegreeNumber(String number) {
        try {
            return Math.abs(Integer.parseInt(number)) < 180;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    private boolean isDegreeChar(char ch) {
        return ch == DEGREE || ch == MASCULINE_ORDINAL || ch == HOURS;
    }

    private boolean isValidDegreeChar(char ch) {
        assert isDegreeChar(ch);
        return ch != MASCULINE_ORDINAL;
    }

    /* Find a minutes match corresponding to a number followed by a minutes symbol */
    @Nullable
    private MatchResult findMinutes(String text, int start) {
        final MatchResult matchNumber = FinderUtils.findNumber(text, start, false, false);
        if (matchNumber == null || matchNumber.end() >= text.length()) {
            // No number to continue searching
            return null;
        }

        if (!isMinuteNumber(matchNumber.group())) {
            return null;
        }

        if (!isPrimeChar(text.charAt(matchNumber.end()))) {
            return null;
        }

        final String space = text.substring(start, matchNumber.start());
        if (!FinderUtils.isBlankOrNonBreakingSpace(space)) {
            // Not a valid space between the previous match and the number match
            return null;
        }

        return matchNumber;
    }

    private boolean isMinuteNumber(String number) {
        try {
            return Integer.parseInt(number) < 60;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    private boolean isPrimeChar(char ch) {
        return PRIME_CHARS.contains(ch);
    }

    private boolean isValidPrimeChar(char ch) {
        return ch == PRIME;
    }

    /*
     * Find a seconds match corresponding to a number followed by a seconds symbol.
     * The seconds symbol is included as a nested match.
     */
    @Nullable
    private MatchResult findSeconds(String text, int start) {
        final FinderMatchResult matchNumber = (FinderMatchResult) FinderUtils.findNumber(text, start, true, false);
        if (matchNumber == null || matchNumber.end() >= text.length()) {
            // No number to continue searching
            return null;
        }

        if (!isSecondNumber(matchNumber.group())) {
            return null;
        }

        // Let's find the seconds symbol, which might have 2 characters.
        String doublePrime;
        final String nextChars = text.substring(matchNumber.end(), Math.min(matchNumber.end() + 2, text.length()));
        if (!nextChars.isEmpty() && DOUBLE_PRIME_CHARS.contains(nextChars.charAt(0))) {
            doublePrime = nextChars.substring(0, 1);
        } else if (nextChars.length() > 1 && isPrimeChar(nextChars.charAt(0)) && isPrimeChar(nextChars.charAt(1))) {
            doublePrime = nextChars;
        } else {
            return null;
        }

        final String space = text.substring(start, matchNumber.start());
        if (!FinderUtils.isBlankOrNonBreakingSpace(space)) {
            // Not a valid space between the previous match and the number match
            return null;
        }

        final MatchResult doublePrimeMatch = FinderMatchResult.of(matchNumber.end(), doublePrime);
        matchNumber.addGroup(doublePrimeMatch);
        return matchNumber;
    }

    private boolean isSecondNumber(String number) {
        // We need to normalize the number in order to parse it as a double
        try {
            return Double.parseDouble(FinderUtils.normalizeDecimalNumber(number)) < 60;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    private boolean isValidDoublePrimeChar(@Nullable String str) {
        return str == null || (str.length() == 1 && str.charAt(0) == DOUBLE_PRIME);
    }

    @Nullable
    private MatchResult findDirection(String text, int start) {
        // Find if there is a cardinal direction and enlarge the match
        final MatchResult matchDirection = FinderUtils.findWordAfter(text, start);
        if (matchDirection == null) {
            return null;
        }

        if (!isDirectionString(matchDirection.group())) {
            return null;
        }

        final String space = text.substring(start, matchDirection.start());
        if (!FinderUtils.isBlankOrNonBreakingSpace(space)) {
            // Not a valid space between the previous match and the number match
            return null;
        }

        return matchDirection;
    }

    private boolean isDirectionString(String str) {
        return CARDINAL_DIRECTIONS.contains(FinderUtils.setFirstUpperCase(str));
    }

    @Override
    public Replacement convert(MatchResult match, FinderPage page) {
        final int matchDegrees = Integer.parseInt(match.group(1));
        final int matchMinutes = Integer.parseInt(match.group(2));
        final String matchSeconds = match.group(3).isEmpty() ? null : match.group(3);
        final String matchDirection = match.group(4).isEmpty() ? null : match.group(4);
        final char matchDegreeSymbol = match.group(5).charAt(0);

        // Fixes
        final StandardType type = matchDegreeSymbol == HOURS ? StandardType.HOURS : StandardType.COORDINATES;
        final char fixedDegreeSymbol = type == StandardType.HOURS ? HOURS : DEGREE;
        final String fixedMinutes = fixMinutes(matchMinutes, type);
        final String fixedSeconds = fixSeconds(matchSeconds, type);

        // Suggestion 1: no spaces
        String noSpaces = String.format(
            "%s%s%s%s%s%s",
            matchDegrees,
            fixedDegreeSymbol,
            fixedMinutes,
            PRIME,
            (matchSeconds == null ? "" : fixedSeconds + DOUBLE_PRIME),
            (matchDirection == null ? "" : NON_BREAKING_SPACE + fixDirection(matchDirection))
        );
        final Suggestion suggestionNoSpaces = Suggestion.of(
            noSpaces,
            "sin espacios y con los símbolos apropiados, recomendado para coordenadas"
        );

        // Suggestion 2: with spaces
        String withSpaces = String.format(
            "{{esd|%s%s %s%s%s%s}}",
            matchDegrees,
            fixedDegreeSymbol,
            fixedMinutes,
            PRIME,
            (matchSeconds == null ? "" : SPACE + fixedSeconds + DOUBLE_PRIME),
            (matchDirection == null ? "" : SPACE + fixDirection(matchDirection))
        );
        final Suggestion suggestionWithSpaces = Suggestion.of(withSpaces, "con espacios y con los símbolos apropiados");

        return Replacement
            .builder()
            .page(page)
            .type(type)
            .start(match.start())
            .text(match.group())
            .suggestions(List.of(suggestionNoSpaces, suggestionWithSpaces))
            .build();
    }

    private String fixMinutes(int minutes, StandardType type) {
        return type == StandardType.HOURS ? fillNumberWithZeros(minutes) : Integer.toString(minutes);
    }

    private String fixSeconds(@Nullable String str, StandardType type) {
        if (StringUtils.isEmpty(str)) {
            return StringUtils.EMPTY;
        }

        // First try to parse them as an integer and then as a double trying to keep the decimal comma if existing
        final int posDot = FinderUtils.normalizeDecimalNumber(str).indexOf(DOT);
        if (posDot < 0) {
            final int seconds = Integer.parseInt(str);
            return type == StandardType.HOURS ? fillNumberWithZeros(seconds) : Integer.toString(seconds);
        } else {
            final int seconds = Integer.parseInt(str.substring(0, posDot));
            final String milliseconds = str.substring(posDot + 1);
            final String secondsStr = type == StandardType.HOURS
                ? fillNumberWithZeros(seconds)
                : Integer.toString(seconds);
            return secondsStr + str.charAt(posDot) + milliseconds;
        }
    }

    private Character fixDirection(String str) {
        final char upperChar = Character.toUpperCase(str.charAt(0));
        return upperChar == 'W' ? 'O' : upperChar;
    }

    private String fillNumberWithZeros(int n) {
        return String.format("%02d", n);
    }
}
