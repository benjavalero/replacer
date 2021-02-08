package es.bvalero.replacer.finder.benchmark.word;

import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

class WordRegexAlternateCompleteFinder implements BenchmarkFinder {

    private final Pattern words;

    WordRegexAlternateCompleteFinder(Collection<String> words) {
        String alternations = "\\b(" + StringUtils.join(words, "|") + ")\\b";
        this.words = Pattern.compile(alternations);
    }

    @Override
    public Set<BenchmarkResult> findMatches(String text) {
        // Build an alternate regex with all the complete words and match it against the text
        Set<BenchmarkResult> matches = new HashSet<>();
        Matcher m = this.words.matcher(text);
        while (m.find()) {
            matches.add(BenchmarkResult.of(m.start(), m.group()));
        }
        return matches;
    }
}
