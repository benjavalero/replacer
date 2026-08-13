package es.bvalero.replacer.finder.benchmark.uppercase;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Loop over all the words and find them in the text with a regex that includes the preceding punctuation.
 * Con: it iterates over all the words, so it may be slow for large collections.
 * Note: cannot be adapted to the standard {@code findMatchResults}/{@code validate} pattern because the punctuation
 * is embedded in the regex pattern itself — the match group includes both punctuation and word.
 */
class UppercaseRegexIterateFinder extends UppercaseBenchmarkFinder {

    private final List<Pattern> words;

    UppercaseRegexIterateFinder(Collection<String> words) {
        this.words = new ArrayList<>();
        for (String word : words) {
            String regex = String.format("(?:%s)\\s*%s", FinderUtils.joinAlternate(PUNCTUATIONS), word);
            this.words.add(Pattern.compile(regex));
        }
    }

    @Override
    public Stream<BenchmarkResult> find(FinderPage page) {
        final String text = page.getContent();
        final List<BenchmarkResult> matches = new ArrayList<>(100);
        for (Pattern word : this.words) {
            final Matcher m = word.matcher(text);
            while (m.find()) {
                String w = m.group().substring(1).trim();
                int pos = m.group().indexOf(w);
                matches.add(BenchmarkResult.of(m.start() + pos, w));
            }
        }
        return matches.stream();
    }
}
