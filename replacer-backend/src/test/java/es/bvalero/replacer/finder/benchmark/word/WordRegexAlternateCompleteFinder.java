package es.bvalero.replacer.finder.benchmark.word;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class WordRegexAlternateCompleteFinder implements BenchmarkFinder {

    private final Pattern pattern;

    WordRegexAlternateCompleteFinder(Collection<String> words) {
        String alternations = "\\b(" + FinderUtils.joinAlternate(words) + ")\\b";
        this.pattern = Pattern.compile(alternations);
    }

    @Override
    public Iterable<BenchmarkResult> find(WikipediaPage page) {
        final String text = page.getContent();
        // Build an alternate regex with all the complete words and match it against the text
        final List<BenchmarkResult> matches = new ArrayList<>(100);
        final Matcher m = this.pattern.matcher(text);
        while (m.find()) {
            final int start = m.start();
            final String word = m.group();
            if (FinderUtils.isWordCompleteInText(start, word, text)) {
                matches.add(BenchmarkResult.of(start, word));
            }
        }
        return matches;
    }
}
