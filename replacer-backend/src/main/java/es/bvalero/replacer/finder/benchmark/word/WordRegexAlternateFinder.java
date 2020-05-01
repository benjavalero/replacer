package es.bvalero.replacer.finder.benchmark.word;

import es.bvalero.replacer.finder.FinderUtils;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.FinderResult;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

class WordRegexAlternateFinder implements BenchmarkFinder {
    private final Pattern words;

    WordRegexAlternateFinder(Collection<String> words) {
        String alternations = '(' + StringUtils.join(words, "|") + ')';
        this.words = Pattern.compile(alternations);
    }

    @Override
    public Set<FinderResult> findMatches(String text) {
        // Build an alternate regex with all the words and match it against the text
        Set<FinderResult> matches = new HashSet<>();
        Matcher m = this.words.matcher(text);
        while (m.find()) {
            if (FinderUtils.isWordCompleteInText(m.start(), m.group(), text)) {
                matches.add(FinderResult.of(m.start(), m.group()));
            }
        }
        return matches;
    }
}
