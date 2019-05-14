package es.bvalero.replacer.finder.benchmark;

import es.bvalero.replacer.finder.MatchResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class CompleteTagRegexLazyLazyFinder extends CompleteTagAbstractFinder {

    private final static List<Pattern> PATTERNS = new ArrayList<>();

    CompleteTagRegexLazyLazyFinder(List<String> words) {
        words.forEach(word -> PATTERNS.add(Pattern.compile(String.format("<%s.*?>.+?</%s>", word, word), Pattern.DOTALL)));
    }

    Set<MatchResult> findMatches(String text) {
        Set<MatchResult> matches = new HashSet<>();
        for (Pattern pattern : PATTERNS) {
            Matcher m = pattern.matcher(text);
            while (m.find()) {
                matches.add(new MatchResult(m.start(), m.group()));
            }
        }
        return matches;
    }

}
