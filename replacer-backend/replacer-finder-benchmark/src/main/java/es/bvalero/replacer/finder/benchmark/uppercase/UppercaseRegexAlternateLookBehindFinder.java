package es.bvalero.replacer.finder.benchmark.uppercase;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public Iterable<BenchmarkResult> find(FinderPage page) {
        final String text = page.getContent();
        // Build an alternate regex with all the words and match it against the text
        final List<BenchmarkResult> matches = new ArrayList<>(100);
        final Matcher m = this.words.matcher(text);
        while (m.find()) {
            String w = m.group().trim();
            int pos = m.group().indexOf(w);
            matches.add(BenchmarkResult.of(m.start() + pos, w));
        }
        return matches;
    }
}
