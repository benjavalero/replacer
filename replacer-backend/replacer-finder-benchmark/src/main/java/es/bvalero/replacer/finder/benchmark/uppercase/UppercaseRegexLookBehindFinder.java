package es.bvalero.replacer.finder.benchmark.uppercase;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public Iterable<BenchmarkResult> find(FinderPage page) {
        final String text = page.getContent();
        // We loop over all the words and find them in the text with a regex
        final List<BenchmarkResult> matches = new ArrayList<>(100);
        for (Pattern word : this.words) {
            final Matcher m = word.matcher(text);
            while (m.find()) {
                String w = m.group().trim();
                int pos = m.group().indexOf(w);
                matches.add(BenchmarkResult.of(m.start() + pos, w));
            }
        }
        return matches;
    }
}
