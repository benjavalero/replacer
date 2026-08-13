package es.bvalero.replacer.finder.benchmark.uppercase;

import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.List;

class UppercaseBenchmarkFinder implements BenchmarkFinder {

    @org.intellij.lang.annotations.RegExp
    private static final String CELL_SEPARATOR = "\\|\\|";

    @org.intellij.lang.annotations.RegExp
    private static final String FIRST_CELL_SEPARATOR = "\n\\|";

    @org.intellij.lang.annotations.RegExp
    private static final String CAPTION_SEPARATOR_REGEX = "\\|\\+";

    // Escaping is necessary for automaton
    private static final String CELL_HTML_TAG = "\\<td\\>";

    private static final String TIMELINE_TEXT = "text:";

    @org.intellij.lang.annotations.RegExp
    private static final String CLASS_PUNCTUATION = "[=#*.!]";

    // Literal versions used by isWordPrecededByPunctuation — copied from UppercaseFinder
    private static final String CAPTION_SEPARATOR = "|+";
    private static final String PARAGRAPH_START = "\n\n";

    static final List<String> PUNCTUATIONS = List.of(
        CLASS_PUNCTUATION,
        FIRST_CELL_SEPARATOR,
        CELL_SEPARATOR,
        CAPTION_SEPARATOR_REGEX,
        CELL_HTML_TAG,
        TIMELINE_TEXT
    );

    // Copied from UppercaseFinder.validate() and its private helpers — keep in sync
    boolean isWordPrecededByPunctuation(int start, String text) {
        int leftTextEnd = start;
        if (FinderUtils.endsAtPosition(text, FinderUtils.START_LINK, leftTextEnd)) {
            leftTextEnd -= FinderUtils.START_LINK.length();
        }
        final int leftTextEndTrimmed = FinderUtils.trimRight(text, 0, leftTextEnd);
        return (
            isPrecededByPunctuation(text, leftTextEndTrimmed) ||
            isPrecededByPipe(text, leftTextEndTrimmed) ||
            isPrecededByCaptionSeparator(text, leftTextEndTrimmed) ||
            isPrecededByParagraphStart(text, leftTextEnd)
        );
    }

    private boolean isPrecededByPunctuation(String text, int end) {
        if (end <= 0) {
            return false;
        }
        final char lastChar = text.charAt(end - 1);
        return (
            lastChar == '=' ||
            lastChar == '#' ||
            lastChar == '*' ||
            lastChar == '>' ||
            lastChar == '.' ||
            lastChar == '!'
        );
    }

    private boolean isPrecededByPipe(String text, int end) {
        if (end <= 0 || text.charAt(end - 1) != FinderUtils.PIPE) {
            return false;
        }
        final int newLinePos = text.lastIndexOf(FinderUtils.NEW_LINE, end - 1);
        final int firstLineCharPos = newLinePos >= 0 ? newLinePos + 1 : 0;
        return text.charAt(firstLineCharPos) == FinderUtils.PIPE;
    }

    private boolean isPrecededByCaptionSeparator(String text, int end) {
        return FinderUtils.endsAtPosition(text, CAPTION_SEPARATOR, end);
    }

    private boolean isPrecededByParagraphStart(String text, int end) {
        return FinderUtils.endsAtPosition(text, PARAGRAPH_START, end);
    }
}
