package es.bvalero.replacer.finder.benchmark.person;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class PersonRegexAlternateCompleteFinder implements BenchmarkFinder {

    private final Pattern words;

    PersonRegexAlternateCompleteFinder(Collection<String> words) {
        String alternations = "(" + FinderUtils.joinAlternate(words) + ")\\s\\p{Lu}";
        this.words = Pattern.compile(alternations);
    }

    @Override
    public Iterable<BenchmarkResult> find(WikipediaPage page) {
        final String text = page.getContent();
        // Build an alternate regex with all the complete words and match it against the text
        final List<BenchmarkResult> matches = new ArrayList<>(100);
        final Matcher m = this.words.matcher(text);
        while (m.find()) {
            matches.add(BenchmarkResult.of(m.start(), m.group().substring(0, m.group().length() - 2)));
        }
        return matches;
    }
}
