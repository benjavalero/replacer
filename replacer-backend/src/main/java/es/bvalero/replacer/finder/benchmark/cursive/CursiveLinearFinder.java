package es.bvalero.replacer.finder.benchmark.cursive;

import es.bvalero.replacer.finder.LinearIterable;
import es.bvalero.replacer.finder.LinearMatcher;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.FinderResult;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import org.apache.commons.collections4.IterableUtils;

class CursiveLinearFinder implements BenchmarkFinder {

    @Override
    public Set<FinderResult> findMatches(String text) {
        return new HashSet<>(IterableUtils.toList(new LinearIterable<>(text, this::findResult, this::convert)));
    }

    public MatchResult findResult(String text, int start) {
        List<MatchResult> matches = new ArrayList<>(100);
        while (start >= 0 && matches.isEmpty()) {
            start = findCursive(text, start, matches);
        }
        return matches.isEmpty() ? null : matches.get(0);
    }

    private int findCursive(String text, int start, List<MatchResult> matches) {
        int startCursive = findStartCursive(text, start);
        if (startCursive >= 0) {
            int numQuotes = findNumQuotes(text, startCursive);
            int endQuotes = findEndQuotes(text, startCursive + numQuotes, numQuotes);
            if (endQuotes >= 0) {
                matches.add(LinearMatcher.of(startCursive, text.substring(startCursive, endQuotes)));
                return endQuotes;
            } else {
                return startCursive + numQuotes;
            }
        } else {
            return -1;
        }
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
            if (ch == '\n') {
                return i + 1;   // To include the linebreak in the benchmark results
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
