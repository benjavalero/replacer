package es.bvalero.replacer.finder.benchmark;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class CursiveRegexDotLazyFinder extends CursiveAbstractFinder {

    private static final String TWO_QUOTES_ONLY = "[^']''[^']";
    private static final String CURSIVE_REGEX = "%s.*?(%s|\n)";
    private static final Pattern CURSIVE_PATTERN = Pattern.compile(String.format(CURSIVE_REGEX, TWO_QUOTES_ONLY, TWO_QUOTES_ONLY));

    Set<FinderResult> findMatches(String text) {
        Set<FinderResult> matches = new HashSet<>();
        Matcher m = CURSIVE_PATTERN.matcher(text);
        while (m.find()) {
            int start = m.start() + 1;
            int end = m.group().endsWith("\n") ? m.group().length() : m.group().length() - 1;
            String group = m.group().substring(1, end);
            matches.add(FinderResult.of(start, group));
        }
        return matches;
    }

}
