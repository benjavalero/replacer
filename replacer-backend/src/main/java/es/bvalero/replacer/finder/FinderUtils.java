package es.bvalero.replacer.finder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class FinderUtils {

    private static final Locale LOCALE_ES = Locale.forLanguageTag("es");
    private static final Set<Character> invalidSeparators = new HashSet<>(Arrays.asList('_', '/'));

    private FinderUtils() {
    }

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

    public static String setFirstUpperCase(String word) {
        return toUpperCase(word.substring(0, 1)) + word.substring(1);
    }

    public static String setFirstUpperCaseClass(String word) {
        if (!startsWithLowerCase(word)) {
            throw new IllegalArgumentException(String.format("Word not starting with lowercase: %s", word));
        }
        return String.format("[%s%s]%s",
                toUpperCase(word.substring(0, 1)),
                word.substring(0, 1),
                word.substring(1));
    }

    public static boolean isWordCompleteInText(int start, String word, String text) {
        int end = start + word.length();
        return start == 0 || end == text.length()
                || (isValidSeparator(text.charAt(start - 1)) && isValidSeparator(text.charAt(end)));
    }

    private static boolean isValidSeparator(char separator) {
        return !Character.isLetterOrDigit(separator) && !invalidSeparators.contains(separator);
    }

}
