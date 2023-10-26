package es.bvalero.replacer.finder.benchmark.cursive;

import static es.bvalero.replacer.finder.util.FinderUtils.NEW_LINE;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.util.regex.MatchResult;
import org.springframework.lang.Nullable;

class CursiveLinearFinder implements BenchmarkFinder {

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return LinearMatchFinder.find(page, this::findCursive);
    }

    @Nullable
    private MatchResult findCursive(FinderPage page, int start) {
        final String text = page.getContent();
        while (start >= 0 && start < text.length()) {
            int startCursive = findStartCursive(text, start);
            if (startCursive >= 0) {
                int numQuotes = findNumQuotes(text, startCursive);
                int endQuotes = findEndQuotes(text, startCursive + numQuotes, numQuotes);
                if (endQuotes >= 0) {
                    return LinearMatchResult.of(text, startCursive, endQuotes);
                } else {
                    start = startCursive + numQuotes;
                }
            } else {
                return null;
            }
        }
        return null;
    }

    private int findStartCursive(String text, int start) {
        return text.indexOf("''", start);
    }

    private int findNumQuotes(String text, int start) {
        StringBuilder quotesBuilder = new StringBuilder();
        for (int i = start; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '\'') {
                quotesBuilder.append(ch);
            } else {
                break;
            }
        }
        return quotesBuilder.length();
    }

    private int findEndQuotes(String text, int start, int numQuotes) {
        StringBuilder tagBuilder = new StringBuilder();
        int i = start;
        for (; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == NEW_LINE) {
                return i + 1; // To include the linebreak in the benchmark results
            } else if (ch == '\'') {
                tagBuilder.append(ch);
            } else {
                if (tagBuilder.length() > numQuotes) {
                    tagBuilder = new StringBuilder(); // Reset and keep on searching
                } else if (tagBuilder.length() == numQuotes) {
                    return i;
                } else if (tagBuilder.length() > 0) {
                    tagBuilder = new StringBuilder(); // Reset and keep on searching
                } // else (tabBuilder.length == 0) ==> continue
            }
        }

        // In case we arrive at the end of the text
        return tagBuilder.length() == numQuotes ? i : -1;
    }
}
