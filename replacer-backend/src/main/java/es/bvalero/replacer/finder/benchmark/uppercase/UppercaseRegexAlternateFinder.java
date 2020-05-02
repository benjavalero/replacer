package es.bvalero.replacer.finder.benchmark.uppercase;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.FinderResult;
import org.apache.commons.lang3.StringUtils;

class UppercaseRegexAlternateFinder implements BenchmarkFinder {
    private final Pattern words;

    UppercaseRegexAlternateFinder(Collection<String> words) {
        String alternations = "[!#*|=.]\\s*(" + StringUtils.join(words, "|") + ')';
        this.words = Pattern.compile(alternations);
    }

    @Override
    public Set<FinderResult> findMatches(String text) {
        // Build an alternate regex with all the words and match it against the text
        Set<FinderResult> matches = new HashSet<>();
        Matcher m = this.words.matcher(text);
        while (m.find()) {
            String w = m.group(1);
            int pos = m.group().indexOf(w);
            matches.add(FinderResult.of(m.start() + pos, w));
        }
        return matches;
    }
}
