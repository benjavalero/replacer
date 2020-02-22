package es.bvalero.replacer.finder.benchmark.domain;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class DomainRegexFinder extends DomainFinder {
    private static final String REGEX_DOMAIN = "[^A-Za-z./_-][A-Za-z.]+\\.[a-z]{2,4}[^a-z]";
    private static final Pattern PATTERN_DOMAIN = Pattern.compile(REGEX_DOMAIN);

    Set<String> findMatches(String text) {
        Set<String> matches = new HashSet<>();
        Matcher m = PATTERN_DOMAIN.matcher(text);
        while (m.find()) {
            matches.add(m.group().substring(1, m.group().length() - 1));
        }
        return matches;
    }
}
