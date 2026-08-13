package es.bvalero.replacer.finder.benchmark.uppercase;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Build a single alternate regex with lookbehind for the punctuation and find the words in the text.
 * Con: lookbehind with alternation can be slow.
 * Note: cannot be adapted to the standard {@code findMatchResults}/{@code validate} pattern because the punctuation
 * is embedded in the regex as a lookbehind — removing it would change the semantics of the strategy being benchmarked.
 */
class UppercaseRegexAlternateLookBehindFinder extends UppercaseBenchmarkFinder {

    private final Pattern words;

    UppercaseRegexAlternateLookBehindFinder(Collection<String> words) {
        String alternations = String.format(
            "(?<=%s)\\s*(?:%s)",
            FinderUtils.joinAlternate(PUNCTUATIONS),
            FinderUtils.joinAlternate(words)
        );
        this.words = Pattern.compile(alternations);
    }

    @Override
    public Stream<BenchmarkResult> find(FinderPage page) {
        final String text = page.getContent();
        final List<BenchmarkResult> matches = new ArrayList<>(100);
        final Matcher m = this.words.matcher(text);
        while (m.find()) {
            String w = m.group().trim();
            int pos = m.group().indexOf(w);
            matches.add(BenchmarkResult.of(m.start() + pos, w));
        }
        return matches.stream();
    }
}
