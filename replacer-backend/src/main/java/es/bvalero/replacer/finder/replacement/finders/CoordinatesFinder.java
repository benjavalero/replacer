package es.bvalero.replacer.finder.replacement.finders;

import static es.bvalero.replacer.finder.util.FinderUtils.NON_BREAKING_SPACE_TEMPLATE;

import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.finder.replacement.ReplacementFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find coordinates with wrong degree, minute or second symbols.
 */
@Component
public class CoordinatesFinder implements ReplacementFinder {

    private static final char DEGREE = '\u00b0'; // °
    private static final char MASCULINE_ORDINAL = '\u00ba'; // º
    private static final Set<Character> DEGREE_CHARS = Set.of(DEGREE, MASCULINE_ORDINAL);
    private static final char PRIME = '\u2032'; // ′
    private static final char APOSTROPHE = '\'';
    private static final char SINGLE_QUOTE = '\u2019'; // ’
    private static final char ACUTE_ACCENT = '\u00b4'; // ´
    private static final Set<Character> PRIME_CHARS = Set.of(PRIME, APOSTROPHE, SINGLE_QUOTE, ACUTE_ACCENT);
    private static final char DOUBLE_PRIME = '\u2033'; // ″
    private static final char DOUBLE_QUOTE = '\"';
    private static final Set<Character> DOUBLE_PRIME_CHARS = Set.of(DOUBLE_PRIME, DOUBLE_QUOTE);
    private static final Set<Character> DECIMAL_SEPARATORS = Set.of('.', ',');
    private static final char[] CARDINAL_DIRECTIONS = new char[] { 'N', 'S', 'W', 'O', 'E' };

    @Override
    public Iterable<MatchResult> findMatchResults(WikipediaPage page) {
        return LinearMatchFinder.find(page, this::findCoordinates);
    }

    private int findCoordinates(WikipediaPage page, int start, List<MatchResult> matches) {
        final String text = page.getContent();

        // Degrees
        final LinearMatchResult matchDegrees = findDegreeMatch(text, start);
        if (matchDegrees == null) {
            return -1;
        }
        final int endDegrees = matchDegrees.end();
        final char degreeChar = text.charAt(endDegrees);
        if (!isDegreeChar(degreeChar)) {
            return -1;
        }

        // Minutes
        final LinearMatchResult matchMinutes = findMinuteMatch(text, endDegrees);
        if (matchMinutes == null) {
            return -1;
        }

        // Prime
        final int endMinutes = matchMinutes.end();
        final char primeChar = text.charAt(endMinutes);
        if (!isPrimeChar(primeChar)) {
            return -1;
        }

        // Seconds
        final LinearMatchResult matchSeconds = findSecondMatch(text, endMinutes);
        if (matchSeconds == null) {
            return -1;
        }

        // Double prime
        final int startDoublePrime = matchSeconds.end();
        if (startDoublePrime >= text.length()) {
            return -1;
        }
        final char doublePrimeChar = text.charAt(startDoublePrime);
        String doublePrime = String.valueOf(doublePrimeChar);
        if (!isDoublePrimeChar(doublePrimeChar)) {
            if (startDoublePrime + 1 >= text.length()) {
                return -1;
            }
            doublePrime = text.substring(startDoublePrime, startDoublePrime + 2);
            if (!isDoublePrime(doublePrime)) {
                return -1;
            }
        }

        // Discard if the coordinates are not to be fixed
        if (isValidDegreeChar(degreeChar) && isValidPrimeChar(primeChar) && isValidDoublePrimeChar(doublePrime)) {
            return -1;
        }

        // Tokens
        final List<LinearMatchResult> coordTokens = new ArrayList<>(List.of(matchDegrees, matchMinutes, matchSeconds));

        // Find if there is a cardinal direction
        int endCoordinates = startDoublePrime + doublePrime.length();
        final LinearMatchResult matchDirection = findDirectionMatch(text, endCoordinates);
        if (matchDirection != null) {
            endCoordinates = matchDirection.end();
            coordTokens.add(matchDirection);
        }

        final int startCoordinates = matchDegrees.start();

        LinearMatchResult coordinatesMatch = LinearMatchResult.of(
            startCoordinates,
            text.substring(startCoordinates, endCoordinates)
        );
        coordinatesMatch.addGroups(coordTokens);
        matches.add(coordinatesMatch);
        return endCoordinates;
    }

    @Nullable
    private LinearMatchResult findDegreeMatch(String text, int start) {
        final LinearMatchResult matchDegrees = findNumberMatch(text, start);
        if (matchDegrees == null) {
            return null;
        } else {
            return isDegreeNumber(matchDegrees.group()) ? matchDegrees : null;
        }
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
                return LinearMatchResult.of(startNumber, text.substring(startNumber, i));
            }
        }
        return null;
    }

    @Override
    public Replacement convert(MatchResult matchResult, WikipediaPage page) {
        LinearMatchResult match = (LinearMatchResult) matchResult;

        final String coordinates = page.getContent().substring(match.start(), match.end());
        String fixedCoordinates = match.group(0) + DEGREE + match.group(1) + PRIME + match.group(2) + DOUBLE_PRIME;

        // Optional cardinal direction
        if (match.groupCount() > 3) {
            fixedCoordinates += NON_BREAKING_SPACE_TEMPLATE + fixDirection(match.group(3));
        }

        return Replacement
            .builder()
            .type(ReplacementType.COORDINATES)
            .start(match.start())
            .text(coordinates)
            .suggestions(List.of(Suggestion.ofNoComment(fixedCoordinates)))
            .build();
    }

    private boolean isDegreeNumber(String number) {
        return StringUtils.isNotEmpty(number) && number.length() <= 3 && Integer.parseInt(number) < 180;
    }

    @Nullable
    private LinearMatchResult findMinuteMatch(String text, int endDegrees) {
        final MatchResult matchMinutes = FinderUtils.findWordAfter(text, endDegrees);
        if (matchMinutes == null) {
            return null;
        } else {
            if (isMinuteNumber(matchMinutes.group())) {
                final String space1 = text.substring(endDegrees + 1, matchMinutes.start());
                if (isSpace(space1)) {
                    return LinearMatchResult.of(matchMinutes);
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    @Nullable
    private LinearMatchResult findSecondMatch(String text, int endMinutes) {
        final MatchResult matchMinutes = FinderUtils.findWordAfter(text, endMinutes, DECIMAL_SEPARATORS);
        if (matchMinutes == null) {
            return null;
        } else {
            if (isSecondNumber(matchMinutes.group())) {
                final String space2 = text.substring(endMinutes + 1, matchMinutes.start());
                if (isSpace(space2)) {
                    return LinearMatchResult.of(matchMinutes);
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    @Nullable
    private LinearMatchResult findDirectionMatch(String text, int endCoordinates) {
        final String textRight = text.substring(endCoordinates);
        final int startDirection = StringUtils.indexOfAny(textRight, CARDINAL_DIRECTIONS);
        if (startDirection >= 0) {
            final String space = textRight.substring(0, startDirection);
            if (isSpace(space)) {
                return LinearMatchResult.of(
                    endCoordinates + startDirection,
                    String.valueOf(textRight.charAt(startDirection))
                );
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private boolean isDegreeChar(char ch) {
        return DEGREE_CHARS.contains(ch);
    }

    private boolean isValidDegreeChar(char ch) {
        return ch == DEGREE;
    }

    private boolean isSpace(String str) {
        return StringUtils.isEmpty(str) || FinderUtils.SPACES.contains(str);
    }

    private boolean isMinuteNumber(String number) {
        return StringUtils.isNotEmpty(number) && number.length() <= 2 && Integer.parseInt(number) < 60;
    }

    private boolean isSecondNumber(String number) {
        return (
            StringUtils.isNotEmpty(number) && number.length() <= 6 && Double.parseDouble(number.replace(',', '.')) < 60
        );
    }

    private boolean isPrimeChar(char ch) {
        return PRIME_CHARS.contains(ch);
    }

    private boolean isValidPrimeChar(char ch) {
        return ch == PRIME;
    }

    private boolean isDoublePrimeChar(char ch) {
        return DOUBLE_PRIME_CHARS.contains(ch);
    }

    private boolean isDoublePrime(String str) {
        return str.length() == 2 && str.chars().mapToObj(c -> (char) c).allMatch(this::isPrimeChar);
    }

    private boolean isValidDoublePrimeChar(String str) {
        return String.valueOf(DOUBLE_PRIME).equals(str);
    }

    private String fixDirection(String str) {
        return "W".equals(str) ? "O" : str;
    }
}