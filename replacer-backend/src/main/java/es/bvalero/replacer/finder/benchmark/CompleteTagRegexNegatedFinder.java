package es.bvalero.replacer.finder.benchmark;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class CompleteTagRegexNegatedFinder extends CompleteTagAbstractFinder {

    private static final List<Pattern> PATTERNS = new ArrayList<>();

    CompleteTagRegexNegatedFinder(List<String> words) {
        words.forEach(word -> PATTERNS.add(Pattern.compile(String.format("<%s[^>]*>.+</%s>", word, word), Pattern.DOTALL)));
    }

    Set<FinderResult> findMatches(String text) {
        Set<FinderResult> matches = new HashSet<>();
        for (Pattern pattern : PATTERNS) {
            Matcher m = pattern.matcher(text);
            while (m.find()) {
                matches.add(FinderResult.of(m.start(), m.group()));
            }
        }
        return matches;
    }

}
