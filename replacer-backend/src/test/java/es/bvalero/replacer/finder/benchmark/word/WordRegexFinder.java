package es.bvalero.replacer.finder.benchmark.word;

import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class WordRegexFinder implements BenchmarkFinder {

    private final List<Pattern> patterns;

    WordRegexFinder(Collection<String> words) {
        this.patterns = new ArrayList<>();
        for (String word : words) {
            this.patterns.add(Pattern.compile(word));
        }
    }

    @Override
    public Set<BenchmarkResult> findMatches(String text) {
        // We loop over all the words and find them in the text with a regex
        Set<BenchmarkResult> matches = new HashSet<>();
        for (Pattern pattern : this.patterns) {
            Matcher m = pattern.matcher(text);
            while (m.find()) {
                if (FinderUtils.isWordCompleteInText(m.start(), m.group(), text)) {
                    matches.add(BenchmarkResult.of(m.start(), m.group()));
                }
            }
        }
        return matches;
    }
}
