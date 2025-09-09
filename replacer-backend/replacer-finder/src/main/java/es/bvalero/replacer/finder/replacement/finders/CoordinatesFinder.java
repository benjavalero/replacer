package es.bvalero.replacer.finder.replacement.finders;

import static es.bvalero.replacer.finder.util.FinderUtils.*;
import static org.apache.commons.lang3.StringUtils.SPACE;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.StandardType;
import es.bvalero.replacer.finder.Suggestion;
import es.bvalero.replacer.finder.replacement.ReplacementFinder;
import es.bvalero.replacer.finder.util.FinderMatchResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.stream.Stream;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find coordinates with wrong degree, minute or second symbols.
 */
@Component
class CoordinatesFinder implements ReplacementFinder {

    private static final char PRIME = '\u2032'; // ′
    private static final char APOSTROPHE = '\'';
    private static final char SINGLE_QUOTE = '\u2019'; // ’
    private static final char ACUTE_ACCENT = '\u00b4'; // ´
    private static final char DOUBLE_PRIME = '\u2033'; // ″
    private static final char DOUBLE_QUOTE = '\"';
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
    public Stream<MatchResult> findMatchResults(FinderPage page) {
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
            // Groups: 1 - Degrees; 2 - Minutes; 3 - Seconds (optional); 4 - Direction (optional)
            result.addGroup(matchDegrees);
            result.addGroup(matchMinutes);
            if (doublePrime != null) {
                result.addGroup(matchSeconds);
            }
            if (matchDirection != null) {
                result.addGroup(matchDirection);
            }
            return result;
        }
        return null;
    }

    /* Find a degrees match corresponding to a number followed by a degree symbol */
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
        return ch == DEGREE || ch == MASCULINE_ORDINAL;
    }

    private boolean isValidDegreeChar(char ch) {
        return ch == DEGREE;
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

        if (!FinderUtils.isBlankOrNonBreakingSpace(text, start, matchNumber.start())) {
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
        return ch == PRIME || ch == APOSTROPHE || ch == SINGLE_QUOTE || ch == ACUTE_ACCENT;
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
        if (!nextChars.isEmpty() && isDoublePrimeChar(nextChars.charAt(0))) {
            doublePrime = nextChars.substring(0, 1);
        } else if (nextChars.length() > 1 && isPrimeChar(nextChars.charAt(0)) && isPrimeChar(nextChars.charAt(1))) {
            doublePrime = nextChars;
        } else {
            return null;
        }

        if (!FinderUtils.isBlankOrNonBreakingSpace(text, start, matchNumber.start())) {
            // Not a valid space between the previous match and the number match
            return null;
        }

        final MatchResult doublePrimeMatch = FinderMatchResult.of(matchNumber.end(), doublePrime);
        matchNumber.addGroup(doublePrimeMatch);
        return matchNumber;
    }

    private boolean isDoublePrimeChar(char ch) {
        return ch == DOUBLE_PRIME || ch == DOUBLE_QUOTE || ch == START_QUOTE_TYPOGRAPHIC || ch == END_QUOTE_TYPOGRAPHIC;
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

        if (!FinderUtils.isBlankOrNonBreakingSpace(text, start, matchDirection.start())) {
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
        String matchSeconds = null;
        String matchDirection = null;
        if (match.groupCount() == 4) {
            matchSeconds = match.group(3);
            matchDirection = match.group(4);
        } else if (match.groupCount() == 3) {
            final String match3 = match.group(3);
            if (isDirectionString(match3)) {
                matchDirection = match3;
            } else {
                matchSeconds = match3;
            }
        }

        // Suggestion 1: no spaces
        String noSpaces =
            String.valueOf(matchDegrees) +
            DEGREE +
            matchMinutes +
            PRIME +
            (matchSeconds == null ? "" : fixSeconds(matchSeconds) + DOUBLE_PRIME) +
            (matchDirection == null ? "" : NON_BREAKING_SPACE + fixDirection(matchDirection));
        final Suggestion suggestionNoSpaces = Suggestion.of(
            noSpaces,
            "sin espacios y con los símbolos apropiados, recomendado para coordenadas"
        );

        // Suggestion 2: with spaces
        String withSpaces =
            "{{esd|" +
            matchDegrees +
            DEGREE +
            SPACE +
            matchMinutes +
            PRIME +
            (matchSeconds == null ? "" : SPACE + matchSeconds + DOUBLE_PRIME) +
            (matchDirection == null ? "" : SPACE + fixDirection(matchDirection)) +
            "}}";
        final Suggestion suggestionWithSpaces = Suggestion.of(withSpaces, "con espacios y con los símbolos apropiados");

        return Replacement.of(
            match.start(),
            match.group(),
            StandardType.COORDINATES,
            List.of(suggestionNoSpaces, suggestionWithSpaces),
            page.getContent()
        );
    }

    private String fixSeconds(String str) {
        // First try to parse them as an integer and then as a double trying to keep the decimal comma if existing
        try {
            return Integer.toString(Integer.parseInt(str));
        } catch (NumberFormatException nfe) {
            final String s = Float.toString(Float.parseFloat(FinderUtils.normalizeDecimalNumber(str)));
            if (str.indexOf(DECIMAL_COMMA) >= 0) {
                return s.replace(DOT, DECIMAL_COMMA);
            } else {
                return s;
            }
        }
    }

    private Character fixDirection(String str) {
        final char upperChar = Character.toUpperCase(str.charAt(0));
        return upperChar == 'W' ? 'O' : upperChar;
    }
}
