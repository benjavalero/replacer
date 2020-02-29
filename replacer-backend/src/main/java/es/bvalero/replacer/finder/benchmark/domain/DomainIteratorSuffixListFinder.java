package es.bvalero.replacer.finder.benchmark.domain;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DomainIteratorSuffixListFinder extends DomainFinder {
    static final Set<Character> START_DOMAIN = new HashSet<>(Arrays.asList('/', '-', '_'));
    static final Set<String> SUFFIXES = new HashSet<>(Arrays.asList("com", "co", "es", "gov", "info", "org"));

    @Override
    public Set<String> findMatches(String text) {
        Set<String> matches = new HashSet<>(100);
        for (String suffix : SUFFIXES) {
            int start = 0;
            while (start >= 0) {
                start = findDomain(text, start, suffix, matches);
            }
        }
        return matches;
    }

    private int findDomain(String text, int start, String suffix, Set<String> matches) {
        int dotSuffix = text.indexOf('.' + suffix, start);
        if (dotSuffix >= 0) {
            // Check valid suffix
            int endDomain = dotSuffix + 1 + suffix.length();
            if (!Character.isLetterOrDigit(text.charAt(endDomain))) {
                int startPrefix = findPrefix(text, dotSuffix - 1);
                if (startPrefix >= 0) {
                    matches.add(text.substring(startPrefix, endDomain));
                }
                return endDomain;
            }
            return endDomain;
        }
        return -1;
    }

    private int findPrefix(String text, int start) {
        for (int i = start; i < text.length(); i--) {
            char ch = text.charAt(i);
            if (!Character.isLetterOrDigit(ch) && ch != '.') {
                return START_DOMAIN.contains(ch) || i == start ? -1 : i + 1;
            }
        }
        return -1;
    }
}
