package es.bvalero.replacer.finder.benchmark.filename;

import es.bvalero.replacer.finder.LinearIterable;
import es.bvalero.replacer.finder.LinearMatcher;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.FinderResult;
import es.bvalero.replacer.page.IndexablePage;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import java.util.*;
import java.util.regex.MatchResult;
import org.apache.commons.collections4.IterableUtils;

class FileLinearFinder implements BenchmarkFinder {

    private static final List<String> ALLOWED_PREFIXES = Arrays.asList("Archivo", "File", "Imagen", "Image");

    @Override
    public Set<FinderResult> findMatches(String text) {
        WikipediaPage page = WikipediaPage.builder().content(text).lang(WikipediaLanguage.getDefault()).build();
        return new HashSet<>(IterableUtils.toList(new LinearIterable<>(page, this::findResult, this::convert)));
    }

    public MatchResult findResult(IndexablePage page, int start) {
        List<MatchResult> matches = new ArrayList<>(100);
        while (start >= 0 && start < page.getContent().length() && matches.isEmpty()) {
            start = findFileName(page.getContent(), start, matches);
        }
        return matches.isEmpty() ? null : matches.get(0);
    }

    private int findFileName(String text, int start, List<MatchResult> matches) {
        int startFile = findStartFile(text, start);
        if (startFile >= 0) {
            int startFileName = findStartFileName(text, startFile + 2);
            if (startFileName >= 0) {
                int endFileName = findEndFile(text, startFileName);
                if (endFileName >= 0) {
                    matches.add(LinearMatcher.of(startFileName, text.substring(startFileName, endFileName)));
                    return endFileName;
                } else {
                    return startFileName;
                }
            } else {
                return startFile + 2;
            }
        } else {
            return -1;
        }
    }

    private int findStartFile(String text, int start) {
        return text.indexOf("[[", start);
    }

    private int findStartFileName(String text, int start) {
        StringBuilder prefixBuilder = new StringBuilder();
        for (int i = start; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '|' || ch == ']') {
                // Not a file but a hyperlink
                return -1;
            } else if (ch == ':') {
                String prefix = prefixBuilder.toString();
                return ALLOWED_PREFIXES.contains(prefix) && (i + 1 < text.length()) ? i + 1 : -1;
            } else {
                prefixBuilder.append(ch);
            }
        }
        return -1;
    }

    private int findEndFile(String text, int start) {
        for (int i = start; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '|' || ch == ']') {
                return i;
            }
        }
        return -1;
    }
}
