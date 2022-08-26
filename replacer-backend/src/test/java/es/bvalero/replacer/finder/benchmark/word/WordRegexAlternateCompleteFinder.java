package es.bvalero.replacer.finder.benchmark.word;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class WordRegexAlternateCompleteFinder implements BenchmarkFinder {

    private final Pattern pattern;

    WordRegexAlternateCompleteFinder(Collection<String> words) {
        String alternations = "\\b(" + FinderUtils.joinAlternate(words) + ")\\b";
        this.pattern = Pattern.compile(alternations);
    }

    @Override
    public Set<BenchmarkResult> findMatches(WikipediaPage page) {
        String text = page.getContent();
        // Build an alternate regex with all the complete words and match it against the text
        Set<BenchmarkResult> matches = new HashSet<>();
        Matcher m = this.pattern.matcher(text);
        while (m.find()) {
            matches.add(BenchmarkResult.of(m.start(), m.group()));
        }
        return matches;
    }
}
