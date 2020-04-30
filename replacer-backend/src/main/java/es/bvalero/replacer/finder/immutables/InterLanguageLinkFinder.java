package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.finder.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;

import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import org.springframework.stereotype.Component;

/**
 * Find inter-language links, e. g. `[[:pt:Title]]`
 */
@Component
public class InterLanguageLinkFinder implements ImmutableFinder {

    @Override
    public int getMaxLength() {
        return 200;
    }

    @Override
    public Iterable<Immutable> find(String text, WikipediaLanguage lang) {
        return new LinearIterable<>(text, this::findResult, this::convert);
    }

    public MatchResult findResult(String text, int start) {
        List<MatchResult> matches = new ArrayList<>(100);
        while (start >= 0 && matches.isEmpty()) {
            start = findLink(text, start, matches);
        }
        return matches.isEmpty() ? null : matches.get(0);
    }

    private int findLink(String text, int start, List<MatchResult> matches) {
        int startLink = findStartLink(text, start);
        if (startLink >= 0) {
            int startLang = startLink + 2;
            if (startLang >= text.length()) {
                return -1;
            } else if (text.charAt(startLang) == ':') {
                startLang++;
            }
            int startInterLanguage = findStartInterLanguage(text, startLang);
            if (startInterLanguage >= 0) {
                int endLink = findEndLink(text, startInterLanguage);
                if (endLink >= 0) {
                    matches.add(LinearMatcher.of(startLink, text.substring(startLink, endLink + 2)));
                    return endLink + 2;
                } else {
                    return startInterLanguage;
                }
            } else {
                return startLink + 2;
            }
        } else {
            return -1;
        }
    }

    private int findStartLink(String text, int start) {
        return text.indexOf("[[", start);
    }

    private int findStartInterLanguage(String text, int start) {
        StringBuilder langBuilder = new StringBuilder();
        for (int i = start; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == ':') {
                String lang = langBuilder.toString();
                return lang.length() > 0 && lang.length() <= 3 ? i + 1 : -1;
            } else if (FinderUtils.isAsciiLowercase(ch)) {
                langBuilder.append(ch);
            } else {
                // Not an inter-language link
                return -1;
            }
        }
        return -1;
    }

    private int findEndLink(String text, int start) {
        return text.indexOf("]]", start);
    }
}
