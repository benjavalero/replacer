package es.bvalero.replacer.finder.benchmark.surname;

import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

class SurnameRegexAlternateCompleteFinder implements BenchmarkFinder {

    private final Pattern words;

    SurnameRegexAlternateCompleteFinder(Collection<String> words) {
        String alternations = "\\p{Lu}\\p{L}+ (" + StringUtils.join(words, "|") + ")";
        this.words = Pattern.compile(alternations);
    }

    @Override
    public Set<BenchmarkResult> findMatches(String text) {
        // Build an alternate regex with all the complete words and match it against the text
        Set<BenchmarkResult> matches = new HashSet<>();
        Matcher m = this.words.matcher(text);
        while (m.find()) {
            int pos = m.group().indexOf(' ') + 1;
            matches.add(BenchmarkResult.of(m.start() + pos, m.group().substring(pos)));
        }
        return matches;
    }
}
