package es.bvalero.replacer.finder.benchmark;

import es.bvalero.replacer.finder.MatchResult;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class CursiveRegexPossessiveFinder extends CursiveAbstractFinder {

    private static final String BOLD_TEMPLATE = "'{3,}[^']++{3,}";
    private final static Pattern CURSIVE_PATTERN = Pattern.compile(String.format("''(%s|[^'\n])++(''|\n)", BOLD_TEMPLATE));

    Set<MatchResult> findMatches(String text) {
        Set<MatchResult> matches = new HashSet<>();
        Matcher m = CURSIVE_PATTERN.matcher(text);
        while (m.find()) {
            matches.add(new MatchResult(m.start(), m.group()));
        }
        return matches;
    }

}
