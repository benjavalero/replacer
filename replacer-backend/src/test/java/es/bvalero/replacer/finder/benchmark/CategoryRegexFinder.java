package es.bvalero.replacer.finder.benchmark;

import es.bvalero.replacer.finder.MatchResult;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class CategoryRegexFinder extends CategoryAbstractFinder {

    private static final String REGEX_CATEGORY = "\\[\\[(Categoría|als):.+?]]";
    private static final Pattern PATTERN_CATEGORY = Pattern.compile(REGEX_CATEGORY);

    Set<MatchResult> findMatches(String text) {
        Set<MatchResult> matches = new HashSet<>();
        Matcher m = PATTERN_CATEGORY.matcher(text);
        while (m.find()) {
            matches.add(MatchResult.of(m.start(), m.group()));
        }
        return matches;
    }

}
