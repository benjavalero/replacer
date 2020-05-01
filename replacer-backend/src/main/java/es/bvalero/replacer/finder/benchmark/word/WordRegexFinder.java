package es.bvalero.replacer.finder.benchmark.word;

import es.bvalero.replacer.finder.FinderUtils;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.FinderResult;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class WordRegexFinder implements BenchmarkFinder {
    private final List<Pattern> words;

    WordRegexFinder(Collection<String> words) {
        this.words = new ArrayList<>();
        for (String word : words) {
            this.words.add(Pattern.compile(word));
        }
    }

    @Override
    public Set<FinderResult> findMatches(String text) {
        // We loop over all the words and find them in the text with a regex
        Set<FinderResult> matches = new HashSet<>();
        for (Pattern word : this.words) {
            Matcher m = word.matcher(text);
            while (m.find()) {
                if (FinderUtils.isWordCompleteInText(m.start(), m.group(), text)) {
                    matches.add(FinderResult.of(m.start(), m.group()));
                }
            }
        }
        return matches;
    }
}
