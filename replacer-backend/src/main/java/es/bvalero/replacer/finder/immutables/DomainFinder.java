package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.finder.FinderUtils;
import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Find web domains, e. g. `www.acb.es` or `es.wikipedia.org`
 */
@Component
public class DomainFinder implements ImmutableFinder {
    static final Set<Character> START_DOMAIN = new HashSet<>(Arrays.asList('/', '-', '_'));
    static final Set<String> SUFFIXES = new HashSet<>(
        Arrays.asList("com", "co", "edu", "es", "gob", "gov", "info", "net", "org")
    );

    @Override
    public Iterable<Immutable> find(String text) {
        List<Immutable> matches = new ArrayList<>(100);
        int start = 0;
        while (start >= 0) {
            start = findDomain(text, start, matches);
        }
        return matches;
    }

    private int findDomain(String text, int start, List<Immutable> matches) {
        int dot = findDot(text, start);
        if (dot >= 0) {
            int endSuffix = findEndSuffix(text, dot + 1);
            if (endSuffix >= 0) {
                int startPrefix = findPrefix(text, dot - 1);
                if (startPrefix >= 0) {
                    matches.add(Immutable.of(startPrefix, text.substring(startPrefix, endSuffix)));
                }
                return endSuffix;
            } else {
                return dot + 1;
            }
        }
        return -1;
    }

    private int findDot(String text, int start) {
        return text.indexOf('.', start);
    }

    private int findEndSuffix(String text, int start) {
        StringBuilder suffixBuilder = new StringBuilder();
        for (int i = start; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (FinderUtils.isAsciiLowercase(ch)) {
                suffixBuilder.append(ch);
            } else {
                return SUFFIXES.contains(suffixBuilder.toString()) ? i : -1;
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
