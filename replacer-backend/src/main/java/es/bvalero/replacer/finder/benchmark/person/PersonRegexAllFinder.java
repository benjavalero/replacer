package es.bvalero.replacer.finder.benchmark.person;

import es.bvalero.replacer.finder.FinderUtils;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import es.bvalero.replacer.finder.benchmark.FinderResult;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class PersonRegexAllFinder implements BenchmarkFinder {
    private final Pattern wordPattern;
    private final Set<String> words;

    PersonRegexAllFinder(Collection<String> words) {
        this.wordPattern = Pattern.compile("\\w++", Pattern.UNICODE_CHARACTER_CLASS);
        this.words = new HashSet<>(words);
    }

    @Override
    public Set<FinderResult> findMatches(String text) {
        // Find all words in the text with a regex and check if they are in the list
        Set<FinderResult> matches = new HashSet<>();
        Matcher m = this.wordPattern.matcher(text);
        while (m.find()) {
            if (this.words.contains(m.group()) && FinderUtils.isWordFollowedByUppercase(m.start(), m.group(), text)) {
                matches.add(FinderResult.of(m.start(), m.group()));
            }
        }
        return matches;
    }
}
