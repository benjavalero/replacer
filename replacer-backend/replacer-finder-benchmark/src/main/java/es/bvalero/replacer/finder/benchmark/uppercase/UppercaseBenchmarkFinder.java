package es.bvalero.replacer.finder.benchmark.uppercase;

import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import java.util.List;

class UppercaseBenchmarkFinder implements BenchmarkFinder {

    @org.intellij.lang.annotations.RegExp
    private static final String CELL_SEPARATOR = "\\|\\|";

    @org.intellij.lang.annotations.RegExp
    private static final String FIRST_CELL_SEPARATOR = "\n\\|";

    @org.intellij.lang.annotations.RegExp
    private static final String CAPTION_SEPARATOR = "\\|\\+";

    // Escaping is necessary for automaton
    private static final String CELL_HTML_TAG = "\\<td\\>";

    private static final String TIMELINE_TEXT = "text:";

    @org.intellij.lang.annotations.RegExp
    private static final String CLASS_PUNCTUATION = "[=#*.!]";

    static final List<String> PUNCTUATIONS = List.of(
        CLASS_PUNCTUATION,
        FIRST_CELL_SEPARATOR,
        CELL_SEPARATOR,
        CAPTION_SEPARATOR,
        CELL_HTML_TAG,
        TIMELINE_TEXT
    );

    static boolean isWordPrecededByPunctuation(int start, String text) {
        List<String> punctuations = List.of("=", "#", "*", ".", "!", "\n|", "||", "|+", "<td>", TIMELINE_TEXT);
        String textBefore = text.substring(0, start).trim();
        return punctuations.stream().anyMatch(textBefore::endsWith);
    }
}
