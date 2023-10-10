package es.bvalero.replacer.finder.replacement.finders;

import static es.bvalero.replacer.finder.util.FinderUtils.*;
import static org.apache.commons.lang3.StringUtils.SPACE;

import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.Suggestion;
import es.bvalero.replacer.finder.replacement.ReplacementFinder;
import es.bvalero.replacer.finder.util.BaseMatchResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find coordinates with wrong degree, minute or second symbols.
 */
@Component
public class CoordinatesFinder implements ReplacementFinder {

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
            final LinearMatchResult matchDegrees = findNumberMatch(text, start);
            if (matchDegrees == null) {
                // No number to continue searching
                return null;
            }
            if (!isDegreeNumber(matchDegrees.group())) {
                start = matchDegrees.end();
                continue;
            }

            // Degree symbol
            final int endDegrees = matchDegrees.end();
            final char degreeChar = text.charAt(endDegrees);
            if (!isDegreeChar(degreeChar)) {
                start = endDegrees + 1;
                continue;
            }

            // Minutes
            final LinearMatchResult matchMinutes = findMinuteMatch(text, endDegrees + 1);
            if (matchMinutes == null) {
                start = endDegrees + 1;
                continue;
            }

            // Prime
            int endCoordinates = matchMinutes.end();
            final char primeChar = text.charAt(endCoordinates);
            if (!isPrimeChar(primeChar)) {
                start = endCoordinates + 1;
                continue;
            } else {
                endCoordinates += 1;
            }

            // Seconds (optional)
            final LinearMatchResult matchSeconds = findSecondMatch(text, endCoordinates);
            String doublePrime = null;
            if (matchSeconds != null) {
                // Double prime
                // Let's find the next 2 characters
                // For the sake of the tests, there could be only 1 character left in the text.
                endCoordinates = matchSeconds.end();
                final String nextSecondChars = text.substring(
                    endCoordinates,
                    Math.min(endCoordinates + 2, text.length())
                );
                doublePrime = findDoublePrime(nextSecondChars);
                if (doublePrime == null) {
                    start = endCoordinates + 1;
                    continue;
                } else {
                    endCoordinates += doublePrime.length();
                }
            }

            // Discard if the coordinates are not to be fixed
            if (isValidDegreeChar(degreeChar) && isValidPrimeChar(primeChar) && isValidDoublePrimeChar(doublePrime)) {
                start = endCoordinates;
                continue;
            }

            // Find if there is a cardinal direction and enlarge the match
            final LinearMatchResult matchDirection = findDirectionMatch(text, endCoordinates);
            if (matchDirection != null) {
                endCoordinates = matchDirection.end();
            }

            final int startCoordinates = matchDegrees.start();
            final LinearMatchResult linearMatch = LinearMatchResult.of(
                startCoordinates,
                text.substring(startCoordinates, endCoordinates)
            );
            final CoordinatesMatchResult result = new CoordinatesMatchResult(linearMatch, page);
            result.setDegrees(matchDegrees.group());
            result.setMinutes(matchMinutes.group());
            if (matchSeconds != null) {
                result.setSeconds(matchSeconds.group());
            }
            if (matchDirection != null) {
                result.setDirection(matchDirection.group());
            }

            return result;
        }
        return null;
    }

    @Nullable
    private LinearMatchResult findNumberMatch(String text, int start) {
        int startNumber = -1;
        for (int i = start; i < text.length(); i++) {
            final char ch = text.charAt(i);
            if (Character.isDigit(ch)) {
                if (startNumber < 0) {
                    startNumber = i;
                }
            } else if (startNumber >= 0) {
                // Capture negative numbers
                if (startNumber >= 1 && text.charAt(startNumber - 1) == '-') {
                    return LinearMatchResult.of(startNumber - 1, text.substring(startNumber - 1, i));
                } else {
                    return LinearMatchResult.of(startNumber, text.substring(startNumber, i));
                }
            }
        }
        return null;
    }

    @Override
    public Replacement convert(MatchResult matchResult, FinderPage page) {
        CoordinatesMatchResult match = (CoordinatesMatchResult) matchResult;

        // Suggestion 1: no spaces
        String noSpaces = String.format(
            "%s%s%s%s%s%s",
            match.getDegrees(),
            DEGREE,
            match.getMinutes(),
            PRIME,
            (match.getSeconds() == null ? "" : match.getSeconds() + DOUBLE_PRIME),
            (match.getDirection() == null ? "" : NON_BREAKING_SPACE + fixDirection(match.getDirection()))
        );
        final Suggestion suggestionNoSpaces = Suggestion.of(
            noSpaces,
            "sin espacios y con los símbolos apropiados, recomendado para coordenadas"
        );

        // Suggestion 2: with spaces
        String withSpaces = String.format(
            "{{esd|%s%s %s%s %s%s}}",
            match.getDegrees(),
            DEGREE,
            match.getMinutes(),
            (match.getSeconds() == null ? "" : match.getSeconds() + DOUBLE_PRIME),
            DOUBLE_PRIME,
            (match.getDirection() == null ? "" : SPACE + fixDirection(match.getDirection()))
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

    @Nullable
    private LinearMatchResult findMinuteMatch(String text, int start) {
        final LinearMatchResult matchMinutes = FinderUtils.findWordAfter(text, start);
        if (matchMinutes == null) {
            return null;
        } else {
            if (isMinuteNumber(matchMinutes.group())) {
                final String space1 = text.substring(start, matchMinutes.start());
                if (FinderUtils.isSpace(space1)) {
                    return matchMinutes;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    @Nullable
    private LinearMatchResult findSecondMatch(String text, int start) {
        final LinearMatchResult matchSeconds = FinderUtils.findWordAfter(text, start, DECIMAL_SEPARATORS);
        if (matchSeconds == null) {
            return null;
        } else {
            if (isSecondNumber(matchSeconds.group())) {
                final String space2 = text.substring(start, matchSeconds.start());
                if (FinderUtils.isSpace(space2)) {
                    return matchSeconds;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    @Nullable
    private LinearMatchResult findDirectionMatch(String text, int start) {
        final LinearMatchResult matchDirection = FinderUtils.findWordAfter(text, start);
        if (matchDirection == null) {
            return null;
        } else {
            if (isDirectionString(matchDirection.group())) {
                final String space3 = text.substring(start, matchDirection.start());
                if (FinderUtils.isSpace(space3)) {
                    return matchDirection;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    private boolean isDegreeNumber(String number) {
        try {
            return Math.abs(Integer.parseInt(number)) < 180;
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
        assert str.length() <= 2;
        // Check first char and then check both chars
        if (DOUBLE_PRIME_CHARS.contains(str.charAt(0))) {
            return str.substring(0, 1);
        } else if (isPrimeChar(str.charAt(0)) && isPrimeChar(str.charAt(1))) {
            return str;
        } else {
            return null;
        }
    }

    private boolean isDirectionString(String str) {
        return CARDINAL_DIRECTIONS.contains(FinderUtils.setFirstUpperCase(str));
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

    private Character fixDirection(String str) {
        final char upperChar = Character.toUpperCase(str.charAt(0));
        return upperChar == 'W' ? 'O' : upperChar;
    }

    @Getter
    @Setter
    private static class CoordinatesMatchResult extends BaseMatchResult {

        private String degrees;
        private String minutes;

        @Nullable
        private String seconds;

        @Nullable
        private String direction;

        protected CoordinatesMatchResult(MatchResult matchResult, FinderPage finderPage) {
            super(matchResult, finderPage);
        }
    }
}
