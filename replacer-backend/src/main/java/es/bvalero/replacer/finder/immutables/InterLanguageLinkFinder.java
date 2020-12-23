package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.finder.*;
import es.bvalero.replacer.page.IndexablePage;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find inter-language links, e.g. `[[pt:Title]]`
 *
 * See https://en.wikipedia.org/wiki/Help:Interlanguage_links
 * Most of these links have been migrated to WikiData but some of them stay for different reasons
 */
@Component
public class InterLanguageLinkFinder implements ImmutableFinder {

    private static final String START_LINK = "[[";
    private static final String END_LINK = "]]";
    // Minimum length: START + lang (2 ch) + : + title (1ch) + END
    private static final int MINIMUM_LENGTH = START_LINK.length() + 4 + END_LINK.length();

    @Override
    public Iterable<Immutable> find(IndexablePage page) {
        return new LinearIterable<>(page, this::findResult, this::convert);
    }

    @Nullable
    public MatchResult findResult(IndexablePage page, int start) {
        List<MatchResult> matches = new ArrayList<>(100);
        while (start >= 0 && start < page.getContent().length() && matches.isEmpty()) {
            start = findLink(page.getContent(), start, matches);
        }
        return matches.isEmpty() ? null : matches.get(0);
    }

    private int findLink(String text, int start, List<MatchResult> matches) {
        int startLink = findStartLink(text, start);
        if (startLink >= 0) {
            // Check text overflow
            if (startLink + MINIMUM_LENGTH >= text.length()) {
                return -1;
            }

            int startLang = startLink + START_LINK.length();
            int posSep = startLang + 2;

            if (text.charAt(posSep) != ':') {
                return posSep + 1;
            }

            String langCode = text.substring(startLang, posSep);
            if (!FinderUtils.isAsciiLowercase(langCode)) {
                return posSep + 1;
            }

            int endLink = findEndLink(text, posSep);
            if (endLink >= 0) {
                String link = text.substring(startLink, endLink + END_LINK.length());
                if (!link.contains("|")) {
                    matches.add(LinearMatcher.of(startLink, link));
                }
                return endLink + END_LINK.length();
            } else {
                return posSep + 1;
            }
        } else {
            return -1;
        }
    }

    private int findStartLink(String text, int start) {
        return text.indexOf(START_LINK, start);
    }

    private int findEndLink(String text, int start) {
        return text.indexOf(END_LINK, start);
    }
}
