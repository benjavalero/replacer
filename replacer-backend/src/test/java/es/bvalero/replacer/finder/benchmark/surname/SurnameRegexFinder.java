package es.bvalero.replacer.finder.benchmark.surname;

import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SurnameRegexFinder implements BenchmarkFinder {

    private final Collection<Pattern> words = new ArrayList<>();

    SurnameRegexFinder(Collection<String> words) {
        for (String word : words) {
            this.words.add(Pattern.compile(word));
        }
    }

    @Override
    public Set<BenchmarkResult> findMatches(String text) {
        // We loop over all the words and find them in the text with a regex
        final Set<BenchmarkResult> matches = new HashSet<>();
        for (Pattern word : this.words) {
            final Matcher m = word.matcher(text);
            while (m.find()) {
                if (FinderUtils.isWordPrecededByUppercase(m.start(), m.group(), text)) {
                    matches.add(BenchmarkResult.of(m.start(), m.group()));
                }
            }
        }
        return matches;
    }
}
