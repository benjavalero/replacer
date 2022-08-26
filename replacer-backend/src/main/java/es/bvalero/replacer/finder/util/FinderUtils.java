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

    private static final Set<Character> INVALID_LEFT_SEPARATORS = Set.of('_', '/', '.');
    private static final Set<Character> INVALID_RIGHT_SEPARATORS = Set.of('_', '/');
    private static final String ALTERNATE_SEPARATOR = "|";
    private static final String NON_BREAKING_SPACE = "&nbsp;";
    private static final String NON_BREAKING_SPACE_TEMPLATE = "{{esd}}";
    public static final Set<String> SPACES = Set.of(SPACE, NON_BREAKING_SPACE, NON_BREAKING_SPACE_TEMPLATE);

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

    public boolean isUpperCase(String word) {
        return word.chars().allMatch(Character::isUpperCase);
    }

    public boolean isAscii(char ch) {
        return (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z');
    }

    public boolean isAsciiLowerCase(String word) {
        return word.chars().allMatch(FinderUtils::isAsciiLowerCase);
    }

    private boolean isAsciiLowerCase(int ch) {
        return (ch >= 'a' && ch <= 'z');
    }

    public boolean isWord(String word) {
        return word.chars().allMatch(Character::isLetter);
    }

    public boolean isNumber(String word) {
        return word.chars().allMatch(Character::isDigit);
    }

    /***** TEXT UTILS *****/

    public boolean isWordCompleteInText(int start, String word, String text) {
        final int end = start + word.length();
        if (start == 0) {
            return end == text.length() || isValidRightSeparator(text.charAt(end));
        } else if (end == text.length()) {
            return isValidLeftSeparator(text, start - 1);
        } else {
            return isValidLeftSeparator(text, start - 1) && isValidRightSeparator(text.charAt(end));
        }
    }

    private boolean isValidLeftSeparator(String text, int position) {
        final char separator = text.charAt(position);
        return (
            !Character.isLetterOrDigit(separator) &&
            !INVALID_LEFT_SEPARATORS.contains(separator) &&
            !isApostrophe(text, position)
        );
    }

    private boolean isApostrophe(String text, int position) {
        return text.charAt(position) == '\'' && Character.isLetterOrDigit(text.charAt(position - 1));
    }

    private boolean isValidRightSeparator(char separator) {
        return !Character.isLetterOrDigit(separator) && !INVALID_RIGHT_SEPARATORS.contains(separator);
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
    public String findWordAfter(int start, String word, String text) {
        final int end = start + word.length();
        if (end + 1 >= text.length() || !Character.isWhitespace(text.charAt(end))) {
            return null;
        }

        int lastLetter = -1;
        for (int i = end + 1; i < text.length(); i++) {
            final char ch = text.charAt(i);
            if (Character.isLetterOrDigit(ch)) {
                lastLetter = i;
            } else {
                break;
            }
        }
        return lastLetter >= 0 ? text.substring(end + 1, lastLetter + 1) : null;
    }

    public boolean isWordPrecededByUpperCase(int start, String text) {
        final String wordBefore = findWordBefore(text, start);
        return wordBefore != null && startsWithUpperCase(wordBefore);
    }

    @Nullable
    public String findWordBefore(String text, int start) {
        if (start < 2 || !Character.isWhitespace(text.charAt(start - 1))) {
            return null;
        }

        int firstLetter = -1;
        for (int i = start - 2; i >= 0; i--) {
            final char ch = text.charAt(i);
            if (Character.isLetterOrDigit(ch)) {
                firstLetter = i;
            } else {
                break;
            }
        }
        return firstLetter >= 0 ? text.substring(firstLetter, start - 1) : null;
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

    public String[] splitAsArray(String text) {
        return StringUtils.split(text);
    }

    public Stream<String> splitAsStream(String text) {
        return Arrays.stream(splitAsArray(text));
    }

    public List<String> splitAsLinkedList(String text) {
        return splitAsStream(text).collect(Collectors.toCollection(LinkedList::new));
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
