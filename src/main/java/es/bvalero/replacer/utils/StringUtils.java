package es.bvalero.replacer.utils;

import es.bvalero.replacer.domain.Interval;
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    private static final String REGEX_PARAGRAPH = "(^|\\n{2,})(.+?)(?=\\n{2,}|$)";
    private static final String REGEX_BUTTON_TAG = "<button.+?</button>";
    private static final String ELLIPSIS = "[...]";
    private static final int THRESHOLD = 200;

    public static String escapeText(String text) {
        return StringEscapeUtils.escapeXml10(text);
    }

    public static String unescapeText(String text) {
        return StringEscapeUtils.unescapeXml(text);
    }

    /*
     * replaceAt('0123456789', 3, '34', 'XXXX') => '012XXXX56789'
     * Throw an exception if the content to replace is not available.
     */
    public static String replaceAt(String text, int position, String replaced, String replacement)
            throws Exception {
        String toReplace = text.substring(position, position + replaced.length());
        if (!toReplace.equals(replaced)) {
            throw new Exception("Cannot replace " + replacement + " in position " + position);
        }

        return text.substring(0, position) + replacement + text.substring(position + replaced.length());
    }

    public static boolean startsWithUpperCase(String word) {
        return Character.isUpperCase(word.charAt(0));
    }

    /* país => País */
    public static String setFirstUpperCase(String word) {
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    /* Removes from the text the paragraphs without misspellings */
    public static String removeParagraphsWithoutMisspellings(String text) {
        Pattern patternParagraph = Pattern.compile(REGEX_PARAGRAPH, Pattern.DOTALL);
        StringBuilder reducedContent = new StringBuilder();

        Matcher matcher = patternParagraph.matcher(text);
        while (matcher.find()) {
            String paragraph = matcher.group(2);
            if (paragraph.contains("miss-")) {
                if (reducedContent.length() != 0) {
                    reducedContent.append("\n<hr>\n");
                }
                reducedContent.append(trimText(paragraph, THRESHOLD));
            }
        }

        return reducedContent.toString();
    }

    static String trimText(String text, int threshold) {
        Pattern patternButton = Pattern.compile(REGEX_BUTTON_TAG, Pattern.DOTALL);
        List<Interval> intervals = new ArrayList<>();
        Matcher matcher = patternButton.matcher(text);
        while (matcher.find()) {
            intervals.add(new Interval(matcher.start(), matcher.end()));
        }

        StringBuilder reducedContent = new StringBuilder();
        int lastFin = 0;
        for (int idx = 0; idx < intervals.size(); idx++) {
            int ini = intervals.get(idx).getStart();
            int fin = intervals.get(idx).getEnd();
            String buttonText = text.substring(ini, fin);
            String textBefore = text.substring(lastFin, ini);
            lastFin = fin;

            if (idx == 0) {
                reducedContent.append(trimRight(textBefore, threshold)).append(buttonText);
            } else {
                reducedContent.append(trimLeftRight(textBefore, threshold)).append(buttonText);
            }
        }

        reducedContent.append(trimLeft(text.substring(lastFin), threshold));

        return reducedContent.toString();
    }

    /* Display only the last n characters of the text, with an ellipsis if needed. */
    private static String trimRight(String text, int threshold) {
        if (text.length() <= threshold) {
            return text;
        } else {
            return ELLIPSIS + ' ' + text.substring(text.length() - threshold, text.length());
        }
    }

    /* Display only the first and last n characters of the text, with an ellipsis if needed. */
    private static String trimLeftRight(String text, int threshold) {
        if (text.length() <= threshold * 2) {
            return text;
        } else {
            return text.substring(0, threshold) + ' ' + ELLIPSIS + ' '
                    + text.substring(text.length() - threshold, text.length());
        }
    }

    /* Display only the first n characters of the text, with an ellipsis if needed. */
    private static String trimLeft(String text, int threshold) {
        if (text.length() <= threshold) {
            return text;
        } else {
            return text.substring(0, threshold) + ' ' + ELLIPSIS;
        }
    }

    /* Add highlight to some places in the text */
    public static String highlightSyntax(String text) {
        String replacedText = text;

        replacedText = replacedText.replaceAll(RegExUtils.REGEX_COMMENT_ESCAPED,
                "<span class=\"syntax comment\">$0</span>");

        replacedText = replacedText.replaceAll(RegExUtils.REGEX_HEADERS,
                "<span class=\"syntax header\">$0</span>");

        replacedText = replacedText.replaceAll(RegExUtils.REGEX_URL,
                "<span class=\"syntax hyperlink\">$0</span>");

        replacedText = replacedText.replaceAll(RegExUtils.REGEX_WIKILINK,
                "<span class=\"syntax link\">$0</span>");

        return replacedText;
    }

}
