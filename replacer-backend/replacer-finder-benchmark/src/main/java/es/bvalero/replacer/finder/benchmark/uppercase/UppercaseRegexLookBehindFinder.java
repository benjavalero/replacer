package es.bvalero.replacer.finder.benchmark.uppercase;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Loop over all the words and find them in the text with a regex using lookbehind for the punctuation.
 * Con: it iterates over all the words, so it may be slow for large collections.
 * Note: cannot be adapted to the standard {@code findMatchResults}/{@code validate} pattern because the punctuation
 * is embedded in the regex as a lookbehind — removing it would change the semantics of the strategy being benchmarked.
 */
class UppercaseRegexLookBehindFinder extends UppercaseBenchmarkFinder {

    private final List<Pattern> words;

    UppercaseRegexLookBehindFinder(Collection<String> words) {
        this.words = new ArrayList<>();
        for (String word : words) {
            String regex = String.format("(?<=%s)\\s*%s", FinderUtils.joinAlternate(PUNCTUATIONS), word);
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
                String w = m.group().trim();
                int pos = m.group().indexOf(w);
                matches.add(BenchmarkResult.of(m.start() + pos, w));
            }
        }
        return matches.stream();
    }
}
