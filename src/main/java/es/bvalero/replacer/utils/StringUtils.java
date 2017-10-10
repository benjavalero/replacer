package es.bvalero.replacer.utils;

import org.apache.commons.lang3.StringEscapeUtils;

public class StringUtils {

    private static final String ELLIPSIS = "[...]";

    public static String escapeText(String text) {
        return StringEscapeUtils.escapeXml10(text);
    }

    public static String unescapeText(String text) {
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
     * @throws IllegalArgumentException If the text doesn't contain the portion to be replaced in the given position.
     */
    public static String replaceAt(String text, int position, String replaced, String replacement)
            throws IllegalArgumentException {
        try {
            String toReplace = text.substring(position, position + replaced.length());
            if (!toReplace.equals(replaced)) {
                String message = "The original replacement doesn't match." +
                        "\nOriginal text: " + text +
                        "\nPosition: " + position +
                        "\nOld replacement: " + replaced +
                        "\nNew replacement: " + replacement;
                throw new IllegalArgumentException(message);
            }

            return text.substring(0, position) + replacement + text.substring(position + replaced.length());
        } catch (Exception e) {
            String message = "Error replacing text." +
                    "\nOriginal text: " + text +
                    "\nPosition: " + position +
                    "\nOld replacement: " + replaced +
                    "\nNew replacement: " + replacement;
            throw new IllegalArgumentException(message, e);
        }
    }

    /**
     * @return If the first letter of the word is uppercase
     */
    public static boolean startsWithUpperCase(String word) {
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
