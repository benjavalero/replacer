package es.bvalero.replacer.finder.benchmark.person;

import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.BenchmarkResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

class PersonRegexAlternateFinder implements BenchmarkFinder {

    private final Pattern words;

    PersonRegexAlternateFinder(Collection<String> words) {
        String alternations = '(' + StringUtils.join(words, "|") + ')';
        this.words = Pattern.compile(alternations);
    }

    @Override
    public Set<BenchmarkResult> findMatches(String text) {
        // Build an alternate regex with all the words and match it against the text
        Set<BenchmarkResult> matches = new HashSet<>();
        Matcher m = this.words.matcher(text);
        while (m.find()) {
            BenchmarkResult match = BenchmarkResult.of(m.start(), m.group());
            if (FinderUtils.isWordFollowedByUppercase(m.start(), m.group(), text)) {
                matches.add(match);
            }
        }
        return matches;
    }
}
