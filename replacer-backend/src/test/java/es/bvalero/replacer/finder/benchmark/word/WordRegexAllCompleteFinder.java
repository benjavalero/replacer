package es.bvalero.replacer.finder.benchmark.word;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class WordRegexAllCompleteFinder implements BenchmarkFinder {

    private final Pattern wordPattern;
    private final Set<String> words;

    WordRegexAllCompleteFinder(Collection<String> words) {
        this.wordPattern = Pattern.compile("\\b\\w+\\b", Pattern.UNICODE_CHARACTER_CLASS);
        this.words = new HashSet<>(words);
    }

    @Override
    public Set<BenchmarkResult> findMatches(FinderPage page) {
        String text = page.getContent();
        // Find all complete words in the text with a regex and check if they are in the list
        Set<BenchmarkResult> matches = new HashSet<>();
        Matcher m = this.wordPattern.matcher(text);
        while (m.find()) {
            if (this.words.contains(m.group())) {
                matches.add(BenchmarkResult.of(m.start(), m.group()));
            }
        }
        return matches;
    }
}
