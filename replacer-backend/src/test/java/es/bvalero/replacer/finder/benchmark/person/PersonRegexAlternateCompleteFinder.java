package es.bvalero.replacer.finder.benchmark.person;

import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

class PersonRegexAlternateCompleteFinder implements BenchmarkFinder {

    private final Pattern words;

    PersonRegexAlternateCompleteFinder(Collection<String> words) {
        String alternations = "(" + StringUtils.join(words, "|") + ")\\s\\p{Lu}";
        this.words = Pattern.compile(alternations);
    }

    @Override
    public Set<BenchmarkResult> findMatches(String text) {
        // Build an alternate regex with all the complete words and match it against the text
        Set<BenchmarkResult> matches = new HashSet<>();
        Matcher m = this.words.matcher(text);
        while (m.find()) {
            matches.add(BenchmarkResult.of(m.start(), m.group().substring(0, m.group().length() - 2)));
        }
        return matches;
    }
}
