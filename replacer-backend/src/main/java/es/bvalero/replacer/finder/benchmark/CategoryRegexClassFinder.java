package es.bvalero.replacer.finder.benchmark;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class CategoryRegexClassFinder extends CategoryAbstractFinder {
    private static final String REGEX_CATEGORY = "\\[\\[(Categoría|als):[^]]+]]";
    private static final Pattern PATTERN_CATEGORY = Pattern.compile(REGEX_CATEGORY);

    Set<FinderResult> findMatches(String text) {
        Set<FinderResult> matches = new HashSet<>();
        Matcher m = PATTERN_CATEGORY.matcher(text);
        while (m.find()) {
            matches.add(FinderResult.of(m.start(), m.group()));
        }
        return matches;
    }
}
