package es.bvalero.replacer.finder.benchmark;

import es.bvalero.replacer.finder.MatchResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class CompleteTagRegexNotLazyNegatedFinder extends CompleteTagAbstractFinder {

    private static final List<Pattern> PATTERNS = new ArrayList<>();

    CompleteTagRegexNotLazyNegatedFinder(List<String> words) {
        words.forEach(word -> PATTERNS.add(Pattern.compile(String.format("<%s[^>]*>.+?</%s>", word, word), Pattern.DOTALL)));
    }

    Set<MatchResult> findMatches(String text) {
        Set<MatchResult> matches = new HashSet<>();
        for (Pattern pattern : PATTERNS) {
            Matcher m = pattern.matcher(text);
            while (m.find()) {
                matches.add(MatchResult.of(m.start(), m.group()));
            }
        }
        return matches;
    }

}
