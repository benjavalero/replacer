package es.bvalero.replacer.finder.util;

import static es.bvalero.replacer.common.util.ReplacerUtils.LOCALE_ES;
import static org.apache.commons.lang3.StringUtils.SPACE;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.common.util.ReplacerUtils;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

@Slf4j
@UtilityClass
public class FinderUtils {

    private static final char MASCULINE_ORDINAL = '\u00ba'; // º
    private static final char UNDERSCORE = '_'; // _ invalid word separator
    private static final Set<Character> URL_SEPARATORS = Set.of('/', '.');
    private static final String ALTERNATE_SEPARATOR = "|";
    private static final String NON_BREAKING_SPACE = "&nbsp;";
    private static final String NON_BREAKING_SPACE_TEMPLATE = "{{esd}}";
    public static final Set<String> SPACES = Set.of(SPACE, NON_BREAKING_SPACE, NON_BREAKING_SPACE_TEMPLATE);
    private static final char DECIMAL_COMMA = ',';
    private static final char DECIMAL_DOT = '.';
    public static final Set<Character> DECIMAL_SEPARATORS = Set.of(DECIMAL_COMMA, DECIMAL_DOT);

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
        return Character.isDigit(word.charAt(0));
    }

    public String setFirstUpperCase(String word) {
        return StringUtils.capitalize(word);
    }

    public String setFirstLowerCase(String word) {
        return StringUtils.uncapitalize(word);
    }

    public String setFirstUpperCaseClass(String word) {
        if (word.length() >= 1) {
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

    private boolean isAsciiLowerCase(int ch) {
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
        return Character.isDigit(ch) || ch == DECIMAL_DOT;
    }

    public String normalizeDecimalNumber(String number) {
        return number.replace(DECIMAL_COMMA, DECIMAL_DOT);
    }

    public boolean isSpace(String str) {
        return StringUtils.isBlank(str) || FinderUtils.SPACES.contains(str);
    }

    /***** TEXT UTILS *****/

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

    private boolean isValidSeparator(char separator) {
        return !Character.isLetterOrDigit(separator) && separator != UNDERSCORE;
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
            end + 1 < text.length() &&
            Character.isWhitespace(text.charAt(end)) &&
            Character.isUpperCase(text.charAt(end + 1))
        );
    }

    @Nullable
    public LinearMatchResult findWordAfter(String text, int endWord) {
        return findWordAfter(text, endWord, Collections.emptyList());
    }

    @Nullable
    public LinearMatchResult findWordAfter(String text, int endWord, Collection<Character> allowedChars) {
        if (endWord >= text.length() || isWordChar(text.charAt(endWord))) {
            return null;
        }

        int firstLetter = -1;
        int lastLetter = -1;
        for (int i = endWord + 1; i < text.length(); i++) {
            final char ch = text.charAt(i);
            if (isWordChar(ch)) {
                if (firstLetter < 0) {
                    firstLetter = i;
                }
                lastLetter = i;
            } else if (firstLetter >= 0 && !allowedChars.contains(ch)) {
                break;
            }
        }
        return firstLetter >= 0 ? LinearMatchResult.of(firstLetter, text.substring(firstLetter, lastLetter + 1)) : null;
    }

    public boolean isWordPrecededByUpperCase(int start, String text) {
        final LinearMatchResult wordBefore = findWordBefore(text, start);
        return wordBefore != null && startsWithUpperCase(wordBefore.group());
    }

    @Nullable
    public LinearMatchResult findWordBefore(String text, int start) {
        return findWordBefore(text, start, Collections.emptyList());
    }

    @Nullable
    public LinearMatchResult findWordBefore(String text, int start, Collection<Character> allowedChars) {
        if (start < 1 || isWordChar(text.charAt(start - 1))) {
            return null;
        }

        int firstLetter = -1;
        int lastLetter = -1;
        for (int i = start - 2; i >= 0; i--) {
            final char ch = text.charAt(i);
            if (isWordChar(ch)) {
                if (lastLetter < 0) {
                    lastLetter = i;
                }
                firstLetter = i;
            } else if (lastLetter >= 0 && !allowedChars.contains(ch)) {
                break;
            }
        }
        return lastLetter >= 0 ? LinearMatchResult.of(firstLetter, text.substring(firstLetter, lastLetter + 1)) : null;
    }

    @Nullable
    public String findFirstWord(String text) {
        int start = -1;
        for (int i = 0; i < text.length(); i++) {
            if (Character.isLetterOrDigit(text.charAt(i))) {
                if (start < 0) {
                    start = i;
                }
            } else if (start >= 0) {
                return text.substring(start, i);
            }
        }
        return start >= 0 ? text.substring(start) : null;
    }

    private boolean isWordChar(char ch) {
        // Unicode considers the masculine ordinal as a letter
        return Character.isLetterOrDigit(ch) && ch != MASCULINE_ORDINAL;
    }

    private String getTextSnippet(String text, int start, int end) {
        return ReplacerUtils.getContextAroundWord(text, start, end, 50);
    }

    /***** COLLECTION UTILS *****/

    public String joinAlternate(Iterable<String> items) {
        return String.join(ALTERNATE_SEPARATOR, items);
    }

    // Get the items in a collection of strings where each string is a comma-separated list itself
    public Set<String> getItemsInCollection(Collection<String> collection) {
        return collection.stream().flatMap(val -> splitList(val).stream()).collect(Collectors.toUnmodifiableSet());
    }

    public Stream<String> splitListAsStream(String list) {
        return Arrays.stream(StringUtils.split(list, ","));
    }

    // Get items in a comma-separated list
    private List<String> splitList(String list) {
        return splitListAsStream(list).collect(Collectors.toUnmodifiableList());
    }

    public String getFirstItemInList(String list) {
        return splitList(list).get(0);
    }

    /***** LOGGING UTILS *****/

    public void logFinderResult(WikipediaPage page, int start, int end, String message) {
        LOGGER.warn(
            "{}: {} - {} - {}",
            message,
            page.getId().getLang(),
            page.getTitle(),
            getTextSnippet(page.getContent(), start, end)
        );
    }
}
