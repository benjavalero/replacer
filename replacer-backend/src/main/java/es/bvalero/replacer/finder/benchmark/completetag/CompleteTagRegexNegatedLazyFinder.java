package es.bvalero.replacer.finder.benchmark.completetag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class CompleteTagRegexNegatedLazyFinder extends CompleteTagFinder {
    private static final List<Pattern> PATTERNS = new ArrayList<>();

    CompleteTagRegexNegatedLazyFinder(List<String> words) {
        words.forEach(
            word -> PATTERNS.add(Pattern.compile(String.format("<%s[^>]*?>.+?</%s>", word, word), Pattern.DOTALL))
        );
    }

    Set<String> findMatches(String text) {
        Set<String> matches = new HashSet<>();
        for (Pattern pattern : PATTERNS) {
            Matcher m = pattern.matcher(text);
            while (m.find()) {
                matches.add(m.group());
            }
        }
        return matches;
    }
}
