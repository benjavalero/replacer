package es.bvalero.replacer.finder.util;

import static es.bvalero.replacer.common.util.ReplacerUtils.LOCALE_ES;
import static org.apache.commons.lang3.StringUtils.SPACE;

import es.bvalero.replacer.common.util.ReplacerUtils;
import es.bvalero.replacer.finder.FinderPage;
import java.util.*;
import java.util.regex.MatchResult;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.lang.Nullable;

@Slf4j
@UtilityClass
public class FinderUtils {

    public static final char MASCULINE_ORDINAL = '\u00ba'; // º
    public static final char FEMININE_ORDINAL = '\u00aa'; // ª
    public static final Set<Character> ORDINALS = Set.of(MASCULINE_ORDINAL, FEMININE_ORDINAL);
    public static final char DEGREE = '\u00b0'; // °
    private static final char UNDERSCORE = '_'; // _ invalid word separator
    private static final Set<Character> URL_SEPARATORS = Set.of('/', '.');
    private static final String ALTERNATE_SEPARATOR = "|";
    public static final String NON_BREAKING_SPACE = "&nbsp;";
    private static final String NON_BREAKING_SPACE_TEMPLATE = "{{esd}}";
    public static final Set<String> SPACES = Set.of(SPACE, NON_BREAKING_SPACE, NON_BREAKING_SPACE_TEMPLATE);
    public static final char NEW_LINE = '\n';
    public static final char PIPE = '|';
    public static final String START_LINK = "[[";
    public static final String END_LINK = "]]";
    public static final char DOT = '.';
    private static final char DECIMAL_COMMA = ',';
    public static final Set<Character> DECIMAL_SEPARATORS = Set.of(DOT, DECIMAL_COMMA);
    private static final Marker MARKER_IMMUTABLE = MarkerFactory.getMarker("IMMUTABLE");
    public static final String ENGLISH_LANGUAGE = "en";

    /***** STRING UTILS *****/

    public String toLowerCase(String text) {
        return text.toLowerCase(LOCALE_ES);
    }

    public String toUpperCase(String text) {
        return text.toUpperCase(LOCALE_ES);
    }

    public boolean startsWithLowerCase(String word) {
        return Character.isLowerCase(word.charAt(0));
    }

    public boolean startsWithUpperCase(String word) {
        return Character.isUpperCase(word.charAt(0));
    }

    public boolean startsWithNumber(String word) {
        return isDigit(word.charAt(0));
    }

    public String setFirstUpperCase(String word) {
        return StringUtils.capitalize(word);
    }

    public String toFirstUpperCase(String word) {
        return toUpperCase(word.substring(0, 1)) + toLowerCase(word.substring(1));
    }

    public String setFirstLowerCase(String word) {
        return StringUtils.uncapitalize(word);
    }

    public String setFirstUpperCaseClass(String word) {
        if (!word.isEmpty()) {
            final char first = word.charAt(0);
            if (Character.isLetter(first)) {
                return String.format(
                    "[%s%s]%s",
                    Character.toUpperCase(first),
                    Character.toLowerCase(first),
                    word.substring(1)
                );
            }
        }
        return word;
    }

    public boolean isAscii(char ch) {
        return (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z');
    }

    public boolean isAsciiLowerCase(String word) {
        for (int i = 0; i < word.length(); i++) {
            if (!isAsciiLowerCase(word.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public boolean isAsciiLowerCase(int ch) {
        return (ch >= 'a' && ch <= 'z');
    }

    public boolean isDecimalNumber(String word) {
        for (int i = 0; i < word.length(); i++) {
            if (!isDecimalNumber(word.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean isDecimalNumber(char ch) {
        return isDigit(ch) || DECIMAL_SEPARATORS.contains(ch);
    }

    public boolean isNumeric(String word) {
        for (int i = 0; i < word.length(); i++) {
            if (!isDigit(word.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public String normalizeDecimalNumber(String number) {
        return number.replace(DECIMAL_COMMA, DOT);
    }

    public boolean isDigit(int ch) {
        return (ch >= '0' && ch <= '9');
    }

    public boolean isBlankOrNonBreakingSpace(String str) {
        return StringUtils.isBlank(str) || isNonBreakingSpace(str);
    }

    public boolean isActualSpace(String str) {
        return SPACES.contains(str);
    }

    public boolean isNonBreakingSpace(String str) {
        return NON_BREAKING_SPACE.equals(str) || NON_BREAKING_SPACE_TEMPLATE.equals(str);
    }

    /***** TEXT UTILS *****/

    /**
     * Check if a word is complete in a text. In particular, check if the characters around the word are separators.
     * In this context, we consider a word separator a character which is not alphanumeric nor an underscore.
     */
    public boolean isWordCompleteInText(int startWord, String word, String text) {
        // We check the separators are not letters. The detected word might not be complete.
        // We check the separators are not digits. There are rare cases where the misspelling
        // is preceded or followed by a digit, e.g. the misspelling "Km" in "Km2".
        // We discard words preceded or followed by certain separators like '_'.
        final int endWord = startWord + word.length();
        if (startWord < 0 || endWord > text.length()) {
            throw new IllegalArgumentException();
        }

        if (startWord >= 1) {
            // Special case: if the start of the word is a separator then we consider it as the left separator itself
            final char leftSeparator;
            final char firstChar = text.charAt(startWord);
            if (isValidSeparator(firstChar)) {
                leftSeparator = firstChar;
            } else {
                leftSeparator = text.charAt(startWord - 1);
            }
            if (endWord < text.length()) {
                // Usual case: word contained in the text
                final char rightSeparator = text.charAt(endWord);
                return isValidSeparator(leftSeparator) && isValidSeparator(rightSeparator);
            } else {
                // Last word in the text with no right separator
                return isValidSeparator(leftSeparator);
            }
        } else if (endWord < text.length()) {
            // First word in the text with no left separator
            final char rightSeparator = text.charAt(endWord);
            return isValidSeparator(rightSeparator);
        }
        return true;
    }

    private boolean isWordChar(char ch) {
        // Unicode considers the masculine/feminine ordinal as a letter
        // We admit the underscore as part of a complete word
        return (Character.isLetterOrDigit(ch) && !isOrdinal(ch)) || ch == UNDERSCORE;
    }

    private boolean isOrdinal(char ch) {
        return ORDINALS.contains(ch);
    }

    private boolean isValidSeparator(char separator) {
        // A word character is not a valid separator and vice versa
        return !isWordChar(separator);
    }

    public boolean isUrlWord(int startWord, String word, String text) {
        final int endWord = startWord + word.length();
        if (startWord <= 0 || endWord >= text.length()) {
            return false;
        }
        final char left = text.charAt(startWord - 1);
        final char right = text.charAt(endWord);
        return left == right && URL_SEPARATORS.contains(left);
    }

    public boolean isWordFollowedByUpperCase(int start, String word, String text) {
        final int end = start + word.length();
        return (
            end + 1 < text.length() && isValidSeparator(text.charAt(end)) && Character.isUpperCase(text.charAt(end + 1))
        );
    }

    /** Find the most close sequence of letters and digits starting at the given position */
    @Nullable
    public LinearMatchResult findWordAfter(String text, int start) {
        return findWordAfter(text, start, List.of(), false);
    }

    /**
     * Find the most close sequence of letters and digits starting at the given position.
     * Some additional chars are allowed, at the start or in the middle according to the configuration.
     */
    @Nullable
    public LinearMatchResult findWordAfter(
        String text,
        int start,
        Collection<Character> allowedChars,
        boolean charsAllowedAtStart
    ) {
        if (start >= text.length()) {
            return null;
        }

        int firstLetter = -1;
        int lastLetter = -1;
        for (int i = start; i < text.length(); i++) {
            final char ch = text.charAt(i);
            if (isWordChar(ch) || (allowedChars.contains(ch) && (firstLetter >= 0 || charsAllowedAtStart))) {
                if (firstLetter < 0) {
                    firstLetter = i;
                }
                lastLetter = i;
            } else if (firstLetter >= 0) {
                break;
            }
        }

        if (firstLetter < 0) {
            return null;
        }

        // Check possible non-breaking space
        final String word = text.substring(firstLetter, lastLetter + 1);
        if (isSpaceWord(word)) {
            return findWordAfter(text, lastLetter + 1, allowedChars, charsAllowedAtStart);
        } else {
            return LinearMatchResult.of(firstLetter, word);
        }
    }

    public int countWords(String text, int start, int end) {
        int count = 0;
        LinearMatchResult matchWord = findWordAfter(text, start);
        while (matchWord != null && matchWord.end() <= end) {
            count++;
            matchWord = findWordAfter(text, matchWord.end());
        }
        return count;
    }

    private boolean isSpaceWord(String word) {
        return "nbsp".equals(word) || "esd".equals(word);
    }

    public boolean isWordPrecededByUpperCase(int start, String text) {
        if (start < 2 || !isValidSeparator(text.charAt(start - 1))) {
            return false;
        }
        final LinearMatchResult wordBefore = findWordBefore(text, start);
        return wordBefore != null && startsWithUpperCase(wordBefore.group());
    }

    /* Find the most close sequence of letters and digits ending at the given position */
    @Nullable
    public LinearMatchResult findWordBefore(String text, int start) {
        return findWordBefore(text, start, List.of(), false);
    }

    /**
     * Find the most close sequence of letters and digits ending at the given position.
     * Some additional chars are allowed, at the start or in the middle according to the configuration.
     */
    @Nullable
    public LinearMatchResult findWordBefore(
        String text,
        int start,
        Collection<Character> allowedChars,
        boolean charsAllowedAtStart
    ) {
        if (start < 1) {
            return null;
        }

        int firstLetter = -1;
        int lastLetter = -1;
        for (int i = start - 1; i >= 0; i--) {
            final char ch = text.charAt(i);
            if (isWordChar(ch) || allowedChars.contains(ch)) {
                if (lastLetter < 0) {
                    lastLetter = i;
                }
                firstLetter = i;
            } else if (lastLetter >= 0) {
                break;
            }
        }

        if (lastLetter < 0) {
            return null;
        }

        if (!charsAllowedAtStart) {
            while (firstLetter < text.length() && allowedChars.contains(text.charAt(firstLetter))) {
                firstLetter++;
            }
            if (firstLetter > lastLetter) {
                return null;
            }
        }

        // Check possible non-breaking space
        final String word = text.substring(firstLetter, lastLetter + 1);
        if (isSpaceWord(word)) {
            return findWordBefore(text, firstLetter, allowedChars, charsAllowedAtStart);
        } else {
            return LinearMatchResult.of(firstLetter, word);
        }
    }

    @Nullable
    public LinearMatchResult findNumberMatch(String text, int start, boolean allowDecimals) {
        int startNumber = -1;
        int endNumber = -1;
        for (int i = start; i < text.length(); i++) {
            final char ch = text.charAt(i);
            if (isDigit(ch) || (allowDecimals && startNumber >= 0 && DECIMAL_SEPARATORS.contains(ch))) {
                if (startNumber < 0) {
                    startNumber = i;
                }
            } else if (startNumber >= 0) {
                endNumber = i;
                break;
            }
        }
        if (startNumber < 0) {
            return null;
        }
        if (endNumber < 0) {
            endNumber = text.length();
        }
        return LinearMatchResult.of(text, startNumber, endNumber);
    }

    private String getTextSnippet(String text, int start, int end) {
        return ReplacerUtils.getContextAroundWord(text, start, end, 50);
    }

    /***** COLLECTION UTILS *****/

    public String joinAlternate(Iterable<String> items) {
        return String.join(ALTERNATE_SEPARATOR, items);
    }

    /***** LOGGING UTILS *****/

    public void logFinderResult(FinderPage page, int start, int end, String message) {
        LOGGER.debug(
            MARKER_IMMUTABLE,
            "{}: {}",
            message,
            ReplacerUtils.toJson(
                "lang",
                page.getPageKey().getLang(),
                "title",
                page.getTitle(),
                "snippet",
                getTextSnippet(page.getContent(), start, end)
            )
        );
    }

    /***** PARSE UTILS *****/

    @FunctionalInterface
    public interface LogResultValidator {
        boolean validate(String text, int start);
    }

    public List<LinearMatchResult> findAllStructures(FinderPage page, String startStr, String endStr) {
        return findAllStructures(page, startStr, endStr, (text, start) -> true);
    }

    public List<LinearMatchResult> findAllStructures(
        FinderPage page,
        String startStr,
        String endStr,
        LogResultValidator logResultValidator
    ) {
        // A loop is a little better than recursion
        final List<LinearMatchResult> matches = new ArrayList<>();

        final String text = page.getContent();
        // Deque implementation is a little better than old stack and recommended by Java
        final Deque<LinearMatchResult> matchStack = new ArrayDeque<>();
        int index = 0;
        while (index >= 0 && index < text.length()) {
            if (matchStack.isEmpty()) {
                final int newStart = text.indexOf(startStr, index);
                if (newStart < 0) {
                    break;
                }
                matchStack.addLast(LinearMatchResult.ofEmpty(newStart));
                index = newStart + startStr.length();
            }

            assert !matchStack.isEmpty();
            final LinearMatchResult currentMatch = matchStack.getLast();
            final int start = currentMatch.start();
            final int end = text.indexOf(endStr, index);
            if (end < 0) {
                // Structure not closed. Not worth keep on searching as the next structures are considered as nested.
                if (logResultValidator.validate(text, start)) {
                    logFinderResult(page, start, start + startStr.length(), "Structure not closed");
                }
                break;
            }

            final int nextStart = text.indexOf(startStr, index);
            if (nextStart >= 0 && nextStart < end) {
                // Nested structure
                final LinearMatchResult nextMatch = LinearMatchResult.ofEmpty(nextStart);
                currentMatch.addGroup(nextMatch);
                matchStack.addLast(nextMatch);
                index = nextStart + startStr.length();
            } else {
                final int actualEnd = end + endStr.length();
                currentMatch.setText(text.substring(start, actualEnd));
                matches.add(currentMatch);
                matchStack.removeLast();
                index = actualEnd;
            }
        }
        return matches;
    }

    public List<MatchResult> findAllWords(String text) {
        final List<MatchResult> words = new ArrayList<>(100);
        int start = 0;
        while (start >= 0 && start < text.length()) {
            // Find start of the word
            int startWord = -1;
            for (int i = start; i < text.length(); i++) {
                if (isWordChar(text.charAt(i))) {
                    startWord = i;
                    break; // Exit for loop
                }
            }
            if (startWord < 0) {
                break; // Exit while loop
            }

            // Find end of the word
            int endWord = text.length(); // Default value
            for (int i = startWord + 1; i < text.length(); i++) {
                if (isValidSeparator(text.charAt(i))) {
                    endWord = i;
                    break; // Exit for loop
                }
            }

            words.add(LinearMatchResult.of(text, startWord, endWord));
            start = endWord + 1;
        }
        return words;
    }
}
