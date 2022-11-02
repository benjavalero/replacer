package es.bvalero.replacer.finder.benchmark.word;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class WordRegexCompleteSeparatorsFinder implements BenchmarkFinder {

    private final List<Pattern> patterns;

    WordRegexCompleteSeparatorsFinder(Collection<String> words) {
        this.patterns = new ArrayList<>();
        for (String word : words) {
            final String leftSeparator = "(?<![\\p{L}\\d_])";
            final String rightSeparator = "(?![\\p{L}\\d_])";
            final String regex = leftSeparator + word + rightSeparator;
            this.patterns.add(Pattern.compile(regex));
        }
    }

    @Override
    public Iterable<BenchmarkResult> find(WikipediaPage page) {
        final String text = page.getContent();
        // We loop over all the words and find them in the text with a regex
        // We cannot use RegexMatchFinder in a loop
        final List<BenchmarkResult> matches = new ArrayList<>(100);
        for (Pattern pattern : this.patterns) {
            final Matcher m = pattern.matcher(text);
            while (m.find()) {
                final int start = m.start();
                final String word = m.group();
                matches.add(BenchmarkResult.of(start, word));
            }
        }
        return matches;
    }
}
