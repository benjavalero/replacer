package es.bvalero.replacer.finder.benchmark.person;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

class PersonRegexAlternateFinder implements BenchmarkFinder {

    private final Pattern words;

    PersonRegexAlternateFinder(Collection<String> words) {
        String alternations = '(' + FinderUtils.joinAlternate(words) + ')';
        this.words = Pattern.compile(alternations);
    }

    @Override
    public Stream<BenchmarkResult> find(FinderPage page) {
        final String text = page.getContent();
        // Build an alternate regex with all the words and match it against the text
        final List<BenchmarkResult> matches = new ArrayList<>(100);
        final Matcher m = this.words.matcher(text);
        while (m.find()) {
            BenchmarkResult match = BenchmarkResult.of(m.start(), m.group());
            if (FinderUtils.isWordFollowedByUpperCase(m.start(), m.group(), text)) {
                matches.add(match);
            }
        }
        return matches.stream();
    }
}
