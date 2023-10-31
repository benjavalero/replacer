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
import java.util.Set;
import java.util.regex.MatchResult;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find coordinates with wrong degree, minute or second symbols.
 */
@Component
class CoordinatesFinder implements ReplacementFinder {

    private static final Set<Character> DEGREE_CHARS = Set.of(DEGREE, MASCULINE_ORDINAL);
    private static final char PRIME = '\u2032'; // ′
    private static final char APOSTROPHE = '\'';
    private static final char SINGLE_QUOTE = '\u2019'; // ’
    private static final char ACUTE_ACCENT = '\u00b4'; // ´
    private static final Set<Character> PRIME_CHARS = Set.of(PRIME, APOSTROPHE, SINGLE_QUOTE, ACUTE_ACCENT);
    private static final char DOUBLE_PRIME = '\u2033'; // ″
    private static final char DOUBLE_QUOTE = '\"';
    private static final Set<Character> DOUBLE_PRIME_CHARS = Set.of(DOUBLE_PRIME, DOUBLE_QUOTE);
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
            // Degrees
            MatchResult matchDegrees = FinderUtils.findNumberMatch(text, start, false);
            if (matchDegrees == null || matchDegrees.end() == text.length()) {
                // No number to continue searching
                return null;
            }
            if (!isDegreeNumber(matchDegrees.group())) {
                start = matchDegrees.end();
                continue;
            }
            int startCoordinates = matchDegrees.start();

            // Degrees can be negative
            if (startCoordinates > 0 && text.charAt(startCoordinates - 1) == '-') {
                startCoordinates -= 1;
                matchDegrees = FinderMatchResult.of(startCoordinates, '-' + matchDegrees.group());
            }

            // Degree symbol
            final int endDegrees = matchDegrees.end();
            final char degreeChar = text.charAt(endDegrees);
            if (!isDegreeChar(degreeChar)) {
                start = endDegrees + 1;
                continue;
            }

            // Minutes
            final MatchResult matchMinutes = FinderUtils.findNumberMatch(text, endDegrees + 1, false);
            if (matchMinutes == null) {
                start = endDegrees + 1;
                continue;
            }
            if (
                !isMinuteNumber(matchMinutes.group()) ||
                !FinderUtils.isBlankOrNonBreakingSpace(text.substring(endDegrees + 1, matchMinutes.start())) ||
                matchMinutes.end() >= text.length()
            ) {
                start = matchDegrees.end();
                continue;
            }

            // Prime
            final int endMinutes = matchMinutes.end();
            final char primeChar = text.charAt(endMinutes);
            if (!isPrimeChar(primeChar)) {
                start = endMinutes + 1;
                continue;
            }

            // Seconds (optional)
            final MatchResult matchSeconds = FinderUtils.findNumberMatch(text, endMinutes + 1, true);
            String doublePrime = null;
            int endCoordinates = endMinutes + 1;
            if (matchSeconds != null) {
                if (
                    isSecondNumber(matchSeconds.group()) &&
                    FinderUtils.isBlankOrNonBreakingSpace(text.substring(endMinutes + 1, matchSeconds.start()))
                ) {
                    // Double prime
                    // Let's find the next 2 characters
                    // For the sake of the tests, there could be only 1 character left in the text.
                    final int endSeconds = matchSeconds.end();
                    final String nextSecondChars = text.substring(endSeconds, Math.min(endSeconds + 2, text.length()));
                    doublePrime = findDoublePrime(nextSecondChars);
                    if (doublePrime != null) {
                        endCoordinates = endSeconds + doublePrime.length();
                    }
                }
            }

            // Discard if the coordinates are not to be fixed
            if (isValidDegreeChar(degreeChar) && isValidPrimeChar(primeChar) && isValidDoublePrimeChar(doublePrime)) {
                start = endCoordinates;
                continue;
            }

            // Find if there is a cardinal direction and enlarge the match
            MatchResult matchDirection = FinderUtils.findWordAfter(text, endCoordinates);
            if (matchDirection != null) {
                if (
                    isDirectionString(matchDirection.group()) &&
                    FinderUtils.isBlankOrNonBreakingSpace(text.substring(endCoordinates, matchDirection.start()))
                ) {
                    endCoordinates = matchDirection.end();
                } else {
                    matchDirection = null;
                }
            }

            final FinderMatchResult result = FinderMatchResult.of(text, startCoordinates, endCoordinates);
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

    private boolean isDegreeNumber(String number) {
        try {
            return Integer.parseInt(number) < 180;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    private boolean isDegreeChar(char ch) {
        return DEGREE_CHARS.contains(ch);
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

    private boolean isSecondNumber(String number) {
        // We need to normalize the number in order to parse it as a double
        try {
            return Double.parseDouble(FinderUtils.normalizeDecimalNumber(number)) < 60;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    @Nullable
    private String findDoublePrime(String str) {
        // Check first char and then check both chars
        if (!str.isEmpty() && DOUBLE_PRIME_CHARS.contains(str.charAt(0))) {
            return str.substring(0, 1);
        } else if (str.length() > 1 && isPrimeChar(str.charAt(0)) && isPrimeChar(str.charAt(1))) {
            return str;
        } else {
            return null;
        }
    }

    private boolean isValidDegreeChar(char ch) {
        return ch == DEGREE;
    }

    private boolean isValidPrimeChar(char ch) {
        return ch == PRIME;
    }

    private boolean isValidDoublePrimeChar(@Nullable String str) {
        return str == null || (str.length() == 1 && str.charAt(0) == DOUBLE_PRIME);
    }

    private boolean isDirectionString(String str) {
        return CARDINAL_DIRECTIONS.contains(FinderUtils.setFirstUpperCase(str));
    }

    @Override
    public Replacement convert(MatchResult match, FinderPage page) {
        final String matchDegrees = match.group(1);
        final String matchMinutes = match.group(2);
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
        String noSpaces = String.format(
            "%s%s%s%s%s%s",
            matchDegrees,
            DEGREE,
            matchMinutes,
            PRIME,
            (matchSeconds == null ? "" : matchSeconds + DOUBLE_PRIME),
            (matchDirection == null ? "" : NON_BREAKING_SPACE + fixDirection(matchDirection))
        );
        final Suggestion suggestionNoSpaces = Suggestion.of(
            noSpaces,
            "sin espacios y con los símbolos apropiados, recomendado para coordenadas"
        );

        // Suggestion 2: with spaces
        String withSpaces = String.format(
            "{{esd|%s%s %s%s %s%s}}",
            matchDegrees,
            DEGREE,
            matchMinutes,
            PRIME,
            (matchSeconds == null ? "" : matchSeconds + DOUBLE_PRIME),
            (matchDirection == null ? "" : SPACE + fixDirection(matchDirection))
        );
        final Suggestion suggestionWithSpaces = Suggestion.of(withSpaces, "con espacios y con los símbolos apropiados");

        return Replacement
            .builder()
            .page(page)
            .type(StandardType.COORDINATES)
            .start(match.start())
            .text(match.group())
            .suggestions(List.of(suggestionNoSpaces, suggestionWithSpaces))
            .build();
    }

    private Character fixDirection(String str) {
        final char upperChar = Character.toUpperCase(str.charAt(0));
        return upperChar == 'W' ? 'O' : upperChar;
    }
}
