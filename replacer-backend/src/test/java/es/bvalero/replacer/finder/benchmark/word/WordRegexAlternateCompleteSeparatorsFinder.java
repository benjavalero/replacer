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

class WordRegexAlternateCompleteSeparatorsFinder implements BenchmarkFinder {

    private final Pattern pattern;

    WordRegexAlternateCompleteSeparatorsFinder(Collection<String> words) {
        final String leftSeparator = "(?<=^|[^\\w\\d_\\/\\.])";
        final String rightSeparator = "(?=$|[^\\w\\d_\\/])";
        final String alternate = "(" + FinderUtils.joinAlternate(words) + ")";
        final String regex = leftSeparator + alternate + rightSeparator;
        this.pattern = Pattern.compile(regex);
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
            if (!FinderUtils.isApostrophe(text, start - 1)) {
                matches.add(BenchmarkResult.of(start, word));
            }
        }
        return matches;
    }
}
