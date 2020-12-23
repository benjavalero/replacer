package es.bvalero.replacer.finder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

public class FinderUtils {

    public static final String STRING_EMPTY = "";
    public static final Locale LOCALE_ES = Locale.forLanguageTag("es");
    private static final Set<Character> invalidSeparators = new HashSet<>(Arrays.asList('_', '/'));

    private FinderUtils() {}

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

    public static boolean containsUppercase(CharSequence text) {
        return text.chars().anyMatch(Character::isUpperCase);
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
            return end == text.length() || isValidSeparator(text.charAt(end));
        } else if (end == text.length()) {
            return isValidSeparator(text.charAt(start - 1));
        } else {
            return isValidSeparator(text.charAt(start - 1)) && isValidSeparator(text.charAt(end));
        }
    }

    private static boolean isValidSeparator(char separator) {
        return !Character.isLetterOrDigit(separator) && !invalidSeparators.contains(separator);
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

    static String getContextAroundWord(String text, int start, int end, int threshold) {
        int limitLeft = Math.max(0, start - threshold);
        int limitRight = Math.min(text.length() - 1, end + threshold);
        return text.substring(limitLeft, limitRight);
    }

    static String escapeRegexCharacters(String regex) {
        return regex.replace(".", "\\.");
    }
}
