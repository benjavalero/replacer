package es.bvalero.replacer.utils;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public class StringUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(StringUtils.class);

    private static final String ELLIPSIS = "[...]";

    private StringUtils() {
    }

    public static String escapeText(String text) {
        return StringEscapeUtils.escapeXml10(text);
    }

    public static String unEscapeText(String text) {
        return StringEscapeUtils.unescapeXml(text);
    }

    /**
     * Replace a text portion.
     * <p>
     * Example: replaceAt('0123456789', 3, '34', 'XXXX') => '012XXXX56789'
     *
     * @param text        The complete text.
     * @param position    The position of the text to be replaced.
     * @param replaced    The text portion to be replaced.
     * @param replacement The replacement for the text portion.
     */
    @Nullable
    public static String replaceAt(@NotNull String text, int position, @NotNull String replaced, @NotNull String replacement) {
        String toReplace = text.substring(position, position + replaced.length());

        if (!toReplace.equals(replaced)) {
            LOGGER.error("The original replacement doesn't match." +
                            "\nOriginal text: {}" +
                            "\nPosition: {}" +
                            "\nOld replacement: {}" +
                            "\nNew replacement: {}",
                    text, position, replaced, replacement);
            return null;
        }

        return text.substring(0, position) + replacement + text.substring(position + replaced.length());
    }

    /**
     * @return If all the characters in the given word are uppercase. Non-alphabetic characters are ignored.
     */
    public static boolean isAllUppercase(@NotNull String word) {
        if (org.apache.commons.lang3.StringUtils.isBlank(word)) {
            return false;
        } else {
            String wordUppercase = word.toUpperCase(Locale.forLanguageTag("es"));
            return word.equals(wordUppercase);
        }
    }

    /**
     * @return If the first letter of the word is uppercase
     */
    public static boolean startsWithUpperCase(@NotNull String word) {
        return Character.isUpperCase(word.charAt(0));
    }

    /**
     * @return The given word turning the first letter into uppercase (if needed)
     */
    public static String setFirstUpperCase(String word) {
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    /**
     * Display only the last n characters of the text, with an ellipsis if needed.
     */
    public static String trimRight(String text, int threshold) {
        if (text.length() <= threshold) {
            return text;
        } else {
            return ELLIPSIS + ' ' + text.substring(text.length() - threshold, text.length());
        }
    }

    /**
     * Display only the first and last n characters of the text, with an ellipsis if needed.
     */
    public static String trimLeftRight(String text, int threshold) {
        if (text.length() <= threshold * 2) {
            return text;
        } else {
            return text.substring(0, threshold) + ' ' + ELLIPSIS + ' '
                    + text.substring(text.length() - threshold, text.length());
        }
    }

    /**
     * Display only the first n characters of the text, with an ellipsis if needed.
     */
    public static String trimLeft(String text, int threshold) {
        if (text.length() <= threshold) {
            return text;
        } else {
            return text.substring(0, threshold) + ' ' + ELLIPSIS;
        }
    }

}
