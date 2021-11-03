package es.bvalero.replacer.finder.util;

import es.bvalero.replacer.finder.FinderPage;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FinderUtils {

    public static final String STRING_EMPTY = "";
    public static final Locale LOCALE_ES = Locale.forLanguageTag("es");
    private static final Set<Character> invalidLeftSeparators = Set.of('_', '/', '.');
    private static final Set<Character> invalidRightSeparators = Set.of('_', '/');
    private static final int CONTEXT_THRESHOLD = 50;

    public static String toLowerCase(String str) {
        return str.toLowerCase(LOCALE_ES);
    }

    private static String toUpperCase(String str) {
        return str.toUpperCase(LOCALE_ES);
    }

    public static boolean startsWithLowerCase(CharSequence word) {
        return Character.isLowerCase(word.charAt(0));
    }

    public static boolean startsWithUpperCase(CharSequence word) {
        return Character.isUpperCase(word.charAt(0));
    }

    public static String setFirstLowerCase(String word) {
        return StringUtils.isBlank(word) ? STRING_EMPTY : toLowerCase(word.substring(0, 1)) + word.substring(1);
    }

    public static String setFirstUpperCase(String word) {
        return StringUtils.isBlank(word) ? STRING_EMPTY : toUpperCase(word.substring(0, 1)) + word.substring(1);
    }

    public static String setFirstUpperCaseClass(String word) {
        String first = word.substring(0, 1);
        return Character.isLetter(first.charAt(0))
            ? String.format("[%s%s]%s", toUpperCase(first), toLowerCase(first), word.substring(1))
            : word;
    }

    public static boolean isWordCompleteInText(int start, String word, String text) {
        if (StringUtils.isBlank(word)) {
            return false;
        }

        int end = start + word.length();
        if (start == 0) {
            return end == text.length() || isValidRightSeparator(text.charAt(end));
        } else if (end == text.length()) {
            return isValidLeftSeparator(text.charAt(start - 1));
        } else {
            return isValidLeftSeparator(text.charAt(start - 1)) && isValidRightSeparator(text.charAt(end));
        }
    }

    private static boolean isValidLeftSeparator(char separator) {
        return !Character.isLetterOrDigit(separator) && !invalidLeftSeparators.contains(separator);
    }

    private static boolean isValidRightSeparator(char separator) {
        return !Character.isLetterOrDigit(separator) && !invalidRightSeparators.contains(separator);
    }

    public static boolean isUppercase(String text) {
        return text.chars().allMatch(Character::isUpperCase);
    }

    public static boolean isAscii(char ch) {
        return (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z');
    }

    public static boolean isAsciiLowercase(String text) {
        return text.chars().allMatch(FinderUtils::isAsciiLowercase);
    }

    private static boolean isAsciiLowercase(int ch) {
        return (ch >= 'a' && ch <= 'z');
    }

    public static boolean isWord(String text) {
        return text.chars().allMatch(Character::isLetter);
    }

    public static boolean isNumber(String text) {
        return text.chars().allMatch(Character::isDigit);
    }

    public static boolean isWordFollowedByUppercase(int start, String word, String text) {
        int upperCasePos = start + word.length() + 1;
        return (
            upperCasePos < text.length() &&
            isWordCompleteInText(start, word, text) &&
            Character.isUpperCase(text.charAt(upperCasePos))
        );
    }

    public static boolean isWordPrecededByUppercase(int start, String word, String text) {
        if (isWordCompleteInText(start, word, text) && start >= 2) {
            char lastLetter = text.charAt(start - 1);
            for (int i = start - 2; i >= 0; i--) {
                char ch = text.charAt(i);
                if (Character.isLetter(ch)) {
                    lastLetter = ch;
                } else {
                    return Character.isUpperCase(lastLetter);
                }
            }
        }
        return false;
    }

    public static String getContextAroundWord(String text, int start, int end, int threshold) {
        int limitLeft = Math.max(0, start - threshold);
        int limitRight = Math.min(text.length(), end + threshold);
        return text.substring(limitLeft, limitRight);
    }

    public static void logWarning(String pageContent, int start, int end, FinderPage page, String message) {
        String immutableText = FinderUtils.getContextAroundWord(pageContent, start, end, CONTEXT_THRESHOLD);
        logWarning(immutableText, page, message);
    }

    public static void logWarning(String matchText, FinderPage page, String message) {
        LOGGER.warn("{}: {} - {} - {}", message, matchText, page.getLang(), page.getTitle());
    }

    /** Get the items in a collection of strings where each string is a comma-separated list itself */
    public static Set<String> getItemsInCollection(Collection<String> collection) {
        return collection.stream().flatMap(val -> splitList(val).stream()).collect(Collectors.toSet());
    }

    private static List<String> splitList(String list) {
        return Arrays.stream(StringUtils.split(list, ",")).collect(Collectors.toList());
    }

    public static String getFirstItemInList(String list) {
        List<String> actualList = splitList(list);
        assert !actualList.isEmpty();
        return actualList.get(0);
    }
}
