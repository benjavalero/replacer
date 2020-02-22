package es.bvalero.replacer.finder.benchmark.domain;

import es.bvalero.replacer.finder.FinderUtils;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DomainLinearFinder extends DomainFinder {

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
        int startDomain = findStartDomain(text, start);
        if (startDomain >= 0) {
            int afterLetters = findLetters(text, startDomain + 1);
            if (afterLetters >= 0) {
                int afterLettersDot = findLettersDot(text, afterLetters);
                if (afterLettersDot >= 0) {
                    if (text.charAt(afterLettersDot - 1) != '.') {
                        matches.add(text.substring(startDomain + 1, afterLettersDot));
                    }
                    return afterLettersDot;
                } else {
                    return afterLetters + 1;
                }
            } else {
                return startDomain + 1;
            }
        } else {
            return -1;
        }
    }

    private int findStartDomain(String text, int start) {
        for (int i = start; i < text.length(); i++) {
            if (isStartDomain(text.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    private int findLetters(String text, int start) {
        for (int i = start; i < text.length(); i++) {
            if (!FinderUtils.isAscii(text.charAt(i))) {
                return i == start ? -1 : i;
            }
        }
        return -1;
    }

    private int findLettersDot(String text, int start) {
        for (int i = start; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (!FinderUtils.isAscii(ch) && ch != '.') {
                return i == start ? -1 : i;
            }
        }
        return -1;
    }

    static final Set<Character> START_DOMAIN = new HashSet<>(Arrays.asList('.', '/', '-'));

    private boolean isStartDomain(char ch) {
        return !FinderUtils.isAscii(ch) && !START_DOMAIN.contains(ch);
    }
}
