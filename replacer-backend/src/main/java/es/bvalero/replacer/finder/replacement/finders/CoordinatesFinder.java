package es.bvalero.replacer.finder.replacement.finders;

import static es.bvalero.replacer.finder.util.FinderUtils.DECIMAL_SEPARATORS;

import es.bvalero.replacer.common.domain.*;
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
    private static final char[] CARDINAL_DIRECTIONS = new char[] { 'N', 'S', 'W', 'O', 'E' };

    @Override
    public Iterable<MatchResult> findMatchResults(WikipediaPage page) {
        return LinearMatchFinder.find(page, this::findCompleteCoordinates);
    }

    @Nullable
    MatchResult findCompleteCoordinates(WikipediaPage page, int start) {
        final String text = page.getContent();
        while (start < text.length()) {
            final LinearMatchResult latitudeMatch = findCoordinates(text, start);
            if (latitudeMatch == null) {
                return null;
            } else {
                final int endLatitude = latitudeMatch.end();
                final LinearMatchResult longitudeMatch = findCoordinates(text, endLatitude);
                if (longitudeMatch == null) {
                    start = endLatitude;
                } else {
                    // Check the space between longitude and latitude
                    final String space = text.substring(endLatitude, longitudeMatch.start());
                    if (FinderUtils.isSpace(space)) {
                        final int startLatitude = latitudeMatch.start();
                        final int endLongitude = longitudeMatch.end();
                        final LinearMatchResult match = LinearMatchResult.of(
                            startLatitude,
                            text.substring(startLatitude, endLongitude)
                        );
                        match.addGroups(latitudeMatch.getGroups());
                        match.addGroups(longitudeMatch.getGroups());
                        return match;
                    } else {
                        // Maybe the longitude is a valid latitude
                        start = endLatitude;
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    private LinearMatchResult findCoordinates(String text, int start) {
        // Degrees
        final LinearMatchResult matchDegrees = findDegreeMatch(text, start);
        if (matchDegrees == null) {
            return null;
        }

        // Degree char
        final int endDegrees = matchDegrees.end();
        final char degreeChar = text.charAt(endDegrees);
        if (!isDegreeChar(degreeChar)) {
            return null;
        }

        // Minutes
        final LinearMatchResult matchMinutes = findMinuteMatch(text, endDegrees);
        if (matchMinutes == null) {
            return null;
        }

        // Prime
        final int endMinutes = matchMinutes.end();
        final char primeChar = text.charAt(endMinutes);
        if (!isPrimeChar(primeChar)) {
            return null;
        }

        // Seconds
        final LinearMatchResult matchSeconds = findSecondMatch(text, endMinutes);
        if (matchSeconds == null) {
            return null;
        }

        // Double prime
        final int startDoublePrime = matchSeconds.end();
        if (startDoublePrime >= text.length()) {
            return null;
        }
        final char doublePrimeChar = text.charAt(startDoublePrime);
        String doublePrime = String.valueOf(doublePrimeChar);
        if (!isDoublePrimeChar(doublePrimeChar)) {
            if (startDoublePrime + 1 >= text.length()) {
                return null;
            }
            doublePrime = text.substring(startDoublePrime, startDoublePrime + 2);
            if (!isDoublePrime(doublePrime)) {
                return null;
            }
        }

        // Cardinal Direction
        int endDoublePrime = startDoublePrime + doublePrime.length();
        final LinearMatchResult matchDirection = findDirectionMatch(text, endDoublePrime);
        if (matchDirection == null) {
            return null;
        }

        final int startCoordinates = matchDegrees.start();
        final int endCoordinates = matchDirection.end();
        final LinearMatchResult match = LinearMatchResult.of(
            startCoordinates,
            text.substring(startCoordinates, endCoordinates)
        );
        match.addGroup(matchDegrees);
        match.addGroup(matchMinutes);
        match.addGroup(matchSeconds);
        match.addGroup(matchDirection);
        return match;
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

        final String coordParams = StringUtils.join(match.getGroupValues(), '|');
        final String fixedCoordinates = "{{Coord|" + coordParams + "}}";

        return Replacement
            .builder()
            .type(ReplacementType.COORDINATES)
            .start(match.start())
            .text(match.group())
            .suggestions(List.of(Suggestion.ofNoComment(fixedCoordinates)))
            .build();
    }

    private boolean isDegreeNumber(String number) {
        return number.length() <= 3 && Integer.parseInt(number) < 180;
    }

    @Nullable
    private LinearMatchResult findMinuteMatch(String text, int endDegrees) {
        final LinearMatchResult matchMinutes = FinderUtils.findWordAfter(text, endDegrees);
        if (matchMinutes == null) {
            return null;
        } else {
            if (isMinuteNumber(matchMinutes.group())) {
                final String space1 = text.substring(endDegrees + 1, matchMinutes.start());
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
    private LinearMatchResult findSecondMatch(String text, int endMinutes) {
        final LinearMatchResult matchSeconds = FinderUtils.findWordAfter(text, endMinutes, DECIMAL_SEPARATORS);
        if (matchSeconds == null) {
            return null;
        } else {
            String normalizedSeconds = FinderUtils.normalizeDecimalNumber(matchSeconds.group());
            if (isSecondNumber(normalizedSeconds)) {
                final String space2 = text.substring(endMinutes + 1, matchSeconds.start());
                if (FinderUtils.isSpace(space2)) {
                    return LinearMatchResult.of(matchSeconds.start(), normalizedSeconds);
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
            if (FinderUtils.isSpace(space)) {
                return LinearMatchResult.of(
                    endCoordinates + startDirection,
                    fixDirection(textRight.charAt(startDirection))
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

    private boolean isMinuteNumber(String number) {
        return (number.length() <= 2 && StringUtils.isNumeric(number) && Integer.parseInt(number) < 60);
    }

    private boolean isSecondNumber(String number) {
        return (number.length() <= 6 && FinderUtils.isDecimalNumber(number) && Double.parseDouble(number) < 60);
    }

    private boolean isPrimeChar(char ch) {
        return PRIME_CHARS.contains(ch);
    }

    private boolean isDoublePrimeChar(char ch) {
        return DOUBLE_PRIME_CHARS.contains(ch);
    }

    private boolean isDoublePrime(String str) {
        if (str.length() != 2) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            if (!isPrimeChar(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private String fixDirection(char ch) {
        final String str = String.valueOf(ch);
        return "O".equals(str) ? "W" : str;
    }
}
