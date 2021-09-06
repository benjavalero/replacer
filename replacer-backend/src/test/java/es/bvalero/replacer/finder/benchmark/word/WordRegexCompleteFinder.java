package es.bvalero.replacer.finder.benchmark.word;

import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class WordRegexCompleteFinder implements BenchmarkFinder {

    private final List<Pattern> patterns;

    WordRegexCompleteFinder(Collection<String> words) {
        this.patterns = new ArrayList<>();
        for (String word : words) {
            this.patterns.add(Pattern.compile("\\b" + word + "\\b"));
        }
    }

    @Override
    public Set<BenchmarkResult> findMatches(String text) {
        // We loop over all the words and find them completely in the text with a regex
        Set<BenchmarkResult> matches = new HashSet<>();
        for (Pattern pattern : this.patterns) {
            Matcher m = pattern.matcher(text);
            while (m.find()) {
                matches.add(BenchmarkResult.of(m.start(), m.group()));
            }
        }
        return matches;
    }
}
