package es.bvalero.replacer.finder.benchmark.word;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Loop over all the words/expressions and find them in the text with a regex.
 * Then the result is checked to be complete in the text.
 */
class WordRegexFinder implements BenchmarkFinder {

    private final List<Pattern> patterns;

    WordRegexFinder(Collection<String> words) {
        this.patterns = new ArrayList<>();
        for (String word : words) {
            this.patterns.add(Pattern.compile(cleanWord(word)));
        }
    }

    @Override
    public Iterable<BenchmarkResult> find(FinderPage page) {
        final String text = page.getContent();
        final List<BenchmarkResult> matches = new ArrayList<>(100);
        for (Pattern pattern : this.patterns) {
            final Matcher m = pattern.matcher(text);
            while (m.find()) {
                final int start = m.start();
                final String word = m.group();
                if (FinderUtils.isWordCompleteInText(start, word, text)) {
                    matches.add(BenchmarkResult.of(start, word));
                }
            }
        }
        return matches;
    }
}
