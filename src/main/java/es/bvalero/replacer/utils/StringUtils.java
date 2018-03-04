package es.bvalero.replacer.utils;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(StringUtils.class);

    private static final String ELLIPSIS = "[...]";
    private static final String REGEX_PARAGRAPH = "(^|\\n{2,})(.+?)(?=\\n{2,}|$)";

    private StringUtils() {
    }

    @NotNull
    public static String escapeText(@NotNull String text) {
        return StringEscapeUtils.escapeXml10(text);
    }

    @NotNull
    public static String unEscapeText(@NotNull String text) {
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
    @NotNull
    public static String setFirstUpperCase(@NotNull String word) {
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    /**
     * Display only the last n characters of the text, with an ellipsis if needed.
     */
    @NotNull
    static String trimRight(@NotNull String text, int threshold) {
        if (text.length() <= threshold) {
            return text;
        } else {
            return ELLIPSIS + ' ' + text.substring(text.length() - threshold, text.length());
        }
    }

    /**
     * Display only the first and last n characters of the text, with an ellipsis if needed.
     */
    @NotNull
    static String trimLeftRight(@NotNull String text, int threshold) {
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
    @NotNull
    static String trimLeft(@NotNull String text, int threshold) {
        if (text.length() <= threshold) {
            return text;
        } else {
            return text.substring(0, threshold) + ' ' + ELLIPSIS;
        }
    }

    /**
     * Trims the text between matches, and also on the left and right sides.
     */
    @NotNull
    public static String trimText(@NotNull String text, int threshold, @NotNull String match) {
        List<RegexMatch> matches = RegExUtils.findMatches(text, match, Pattern.DOTALL);

        StringBuilder reducedContent = new StringBuilder();
        int lastMatchEnd = 0;
        for (int i = 0; i < matches.size(); i++) {
            int matchStart = matches.get(i).getPosition();
            int matchEnd = matches.get(i).getEnd();
            String matchText = text.substring(matchStart, matchEnd);
            String textBeforeMatch = text.substring(lastMatchEnd, matchStart);
            lastMatchEnd = matchEnd;

            if (i == 0) {
                reducedContent.append(StringUtils.trimRight(textBeforeMatch, threshold)).append(matchText);
            } else {
                reducedContent.append(StringUtils.trimLeftRight(textBeforeMatch, threshold)).append(matchText);
            }
        }

        reducedContent.append(StringUtils.trimLeft(text.substring(lastMatchEnd), threshold));
        return reducedContent.toString();
    }

    /**
     * @return The text blocks (paragraphs) not containing the match.
     */
    @NotNull
    public static List<String> removeParagraphsNotMatching(@NotNull String text, @NotNull String match) {
        Pattern patternParagraph = Pattern.compile(REGEX_PARAGRAPH, Pattern.DOTALL);
        Pattern patternMatch = Pattern.compile(match);

        List<String> matchingParagraphs = new ArrayList<>();

        Matcher matcherParagraph = patternParagraph.matcher(text);
        while (matcherParagraph.find()) {
            String paragraph = matcherParagraph.group(2);
            Matcher matcherMatch = patternMatch.matcher(paragraph);
            if (matcherMatch.find()) {
                matchingParagraphs.add(paragraph);
            }
        }

        return matchingParagraphs;
    }

}
