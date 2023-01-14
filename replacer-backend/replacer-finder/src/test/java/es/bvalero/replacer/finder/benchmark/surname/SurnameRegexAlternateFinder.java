package es.bvalero.replacer.finder.benchmark.surname;

import es.bvalero.replacer.finder.BenchmarkResult;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SurnameRegexAlternateFinder implements BenchmarkFinder {

    private final Pattern words;

    SurnameRegexAlternateFinder(Collection<String> words) {
        final String alternations = '(' + FinderUtils.joinAlternate(words) + ')';
        this.words = Pattern.compile(alternations);
    }

    @Override
    public Iterable<BenchmarkResult> find(FinderPage page) {
        final String text = page.getContent();
        // Build an alternate regex with all the words and match it against the text
        final List<BenchmarkResult> matches = new ArrayList<>(100);
        final Matcher m = this.words.matcher(text);
        while (m.find()) {
            final BenchmarkResult match = BenchmarkResult.of(m.start(), m.group());
            if (FinderUtils.isWordPrecededByUpperCase(m.start(), text)) {
                matches.add(match);
            }
        }
        return matches;
    }
}
