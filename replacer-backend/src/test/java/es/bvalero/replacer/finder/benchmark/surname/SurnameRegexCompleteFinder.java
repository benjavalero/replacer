package es.bvalero.replacer.finder.benchmark.surname;

import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.FinderResult;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SurnameRegexCompleteFinder implements BenchmarkFinder {
    private final List<Pattern> words;

    SurnameRegexCompleteFinder(Collection<String> words) {
        this.words = new ArrayList<>();
        for (String word : words) {
            this.words.add(Pattern.compile("\\p{Lu}\\p{L}+ " + word));
        }
    }

    @Override
    public Set<FinderResult> findMatches(String text) {
        // We loop over all the words and find them completely in the text with a regex
        Set<FinderResult> matches = new HashSet<>();
        for (Pattern word : this.words) {
            Matcher m = word.matcher(text);
            while (m.find()) {
                int pos = m.group().indexOf(' ') + 1;
                matches.add(FinderResult.of(m.start() + pos, m.group().substring(pos)));
            }
        }
        return matches;
    }
}
