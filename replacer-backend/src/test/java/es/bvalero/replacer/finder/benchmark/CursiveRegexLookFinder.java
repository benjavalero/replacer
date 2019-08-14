package es.bvalero.replacer.finder.benchmark;

import es.bvalero.replacer.finder.MatchResult;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class CursiveRegexLookFinder extends CursiveAbstractFinder {

    private static final String TWO_QUOTES_ONLY = "(?<!')''(?!')";
    private static final String CURSIVE_REGEX = "%s(('''''|'''|')?[^'\n])*(%s|\n)";
    private static final Pattern CURSIVE_PATTERN = Pattern.compile(String.format(CURSIVE_REGEX, TWO_QUOTES_ONLY, TWO_QUOTES_ONLY));

    Set<MatchResult> findMatches(String text) {
        Set<MatchResult> matches = new HashSet<>();
        Matcher m = CURSIVE_PATTERN.matcher(text);
        while (m.find()) {
            matches.add(MatchResult.of(m.start(), m.group()));
        }
        return matches;
    }

}
