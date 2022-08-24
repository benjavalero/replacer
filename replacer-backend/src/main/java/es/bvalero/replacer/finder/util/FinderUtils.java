package es.bvalero.replacer.finder.util;

import static es.bvalero.replacer.common.util.ReplacerUtils.LOCALE_ES;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.common.util.ReplacerUtils;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

@Slf4j
@UtilityClass
public class FinderUtils {

    public static final String STRING_EMPTY = "";
    private static final Set<Character> invalidLeftSeparators = Set.of('_', '/', '.');
    private static final Set<Character> invalidRightSeparators = Set.of('_', '/');
    private static final int CONTEXT_THRESHOLD = 50;

    public String toLowerCase(String str) {
        return str.toLowerCase(LOCALE_ES);
    }

    private String toUpperCase(String str) {
        return str.toUpperCase(LOCALE_ES);
    }

    public boolean startsWithLowerCase(CharSequence word) {
        return Character.isLowerCase(word.charAt(0));
    }

    public boolean startsWithUpperCase(CharSequence word) {
        return Character.isUpperCase(word.charAt(0));
    }

    public boolean startsWithNumber(CharSequence word) {
        return Character.isDigit(word.charAt(0));
    }

    public String setFirstLowerCase(String word) {
        return StringUtils.isBlank(word) ? STRING_EMPTY : toLowerCase(word.substring(0, 1)) + word.substring(1);
    }

    public String setFirstUpperCase(String word) {
        return StringUtils.isBlank(word) ? STRING_EMPTY : toUpperCase(word.substring(0, 1)) + word.substring(1);
    }

    public String setFirstUpperCaseClass(String word) {
        final String first = word.substring(0, 1);
        return Character.isLetter(first.charAt(0))
            ? String.format("[%s%s]%s", toUpperCase(first), toLowerCase(first), word.substring(1))
            : word;
    }

    public boolean isWordCompleteInText(int start, String word, String text) {
        if (StringUtils.isBlank(word)) {
            return false;
        }

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
            !invalidLeftSeparators.contains(separator) &&
            !isApostrophe(text, position)
        );
    }

    private boolean isApostrophe(String text, int position) {
        return text.charAt(position) == '\'' && Character.isLetterOrDigit(text.charAt(position - 1));
    }

    private boolean isValidRightSeparator(char separator) {
        return !Character.isLetterOrDigit(separator) && !invalidRightSeparators.contains(separator);
    }

    public boolean isUppercase(String text) {
        return text.chars().allMatch(Character::isUpperCase);
    }

    public boolean isAscii(char ch) {
        return (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z');
    }

    public boolean isAsciiLowercase(String text) {
        return text.chars().allMatch(FinderUtils::isAsciiLowercase);
    }

    private boolean isAsciiLowercase(int ch) {
        return (ch >= 'a' && ch <= 'z');
    }

    public boolean isWord(String text) {
        return text.chars().allMatch(Character::isLetter);
    }

    public boolean isNumber(String text) {
        return text.chars().allMatch(Character::isDigit);
    }

    public boolean isWordFollowedByUppercase(int start, String word, String text) {
        final int upperCasePos = start + word.length() + 1;
        return (
            upperCasePos < text.length() &&
            isWordCompleteInText(start, word, text) &&
            Character.isUpperCase(text.charAt(upperCasePos))
        );
    }

    public boolean isWordPrecededByUppercase(int start, String word, String text) {
        if (isWordCompleteInText(start, word, text) && start >= 2) {
            char lastLetter = text.charAt(start - 1);
            for (int i = start - 2; i >= 0; i--) {
                final char ch = text.charAt(i);
                if (Character.isLetter(ch)) {
                    lastLetter = ch;
                } else {
                    return Character.isUpperCase(lastLetter);
                }
            }
        }
        return false;
    }

    @Nullable
    public String findWordAfter(String text, int start) {
        if (text.length() - start < 2 || Character.isLetterOrDigit(text.charAt(start))) {
            return null;
        }

        int lastLetter = -1;
        for (int i = start + 1; i < text.length(); i++) {
            final char ch = text.charAt(i);
            if (Character.isLetterOrDigit(ch)) {
                lastLetter = i;
            } else {
                break;
            }
        }
        return lastLetter >= 0 ? text.substring(start + 1, lastLetter + 1) : null;
    }

    @Nullable
    public String findWordBefore(String text, int start) {
        if (start < 2 || Character.isLetterOrDigit(text.charAt(start - 1))) {
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

    private String getPageSnippet(WikipediaPage page, int start, int end) {
        return ReplacerUtils.getContextAroundWord(page.getContent(), start, end, CONTEXT_THRESHOLD);
    }

    public void logFinderResult(WikipediaPage page, int start, int end, String message) {
        LOGGER.warn(
            "{}: {} - {} - {}",
            message,
            page.getId().getLang(),
            page.getTitle(),
            getPageSnippet(page, start, end)
        );
    }

    /** Get the items in a collection of strings where each string is a comma-separated list itself */
    public Set<String> getItemsInCollection(Collection<String> collection) {
        return collection.stream().flatMap(val -> splitList(val).stream()).collect(Collectors.toSet());
    }

    private List<String> splitList(String list) {
        return Arrays.stream(StringUtils.split(list, ",")).collect(Collectors.toList());
    }

    public String getFirstItemInList(String list) {
        final List<String> actualList = splitList(list);
        assert !actualList.isEmpty();
        return actualList.get(0);
    }

    public String getFirstWord(String text) {
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
        return start >= 0 ? text : "";
    }
}
