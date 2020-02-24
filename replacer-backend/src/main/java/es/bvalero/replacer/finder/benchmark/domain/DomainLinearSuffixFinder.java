package es.bvalero.replacer.finder.benchmark.domain;

import es.bvalero.replacer.finder.FinderUtils;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DomainLinearSuffixFinder extends DomainFinder {
    static final Set<Character> START_DOMAIN = new HashSet<>(Arrays.asList('/', '-', '_'));

    @Override
    public Set<String> findMatches(String text) {
        Set<String> matches = new HashSet<>(100);
        int start = 0;
        while (start >= 0) {
            start = findDomain(text, start, matches);
        }
        return matches;
    }

    private int findDomain(String text, int start, Set<String> matches) {
        int dot = text.indexOf('.', start);
        if (dot >= 0) {
            int endSuffix = findSuffix(text, dot + 1);
            if (endSuffix >= 0) {
                int startPrefix = findPrefix(text, dot - 1);
                if (startPrefix >= 0) {
                    matches.add(text.substring(startPrefix, endSuffix));
                    return endSuffix;
                } else {
                    return dot + 1;
                }
            } else {
                return dot + 1;
            }
        }
        return -1;
    }

    private int findSuffix(String text, int start) {
        for (int i = start; i < text.length(); i++) {
            if (!FinderUtils.isAsciiLowercase(text.charAt(i))) {
                int length = i - start;
                return length >= 2 && length <= 4 ? i : -1;
            }
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
