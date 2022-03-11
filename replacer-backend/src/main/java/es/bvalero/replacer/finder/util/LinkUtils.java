package es.bvalero.replacer.finder.util;

import es.bvalero.replacer.common.domain.WikipediaPage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.springframework.lang.Nullable;

@UtilityClass
public class LinkUtils {

    public static final String START_LINK = "[[";
    public static final String END_LINK = "]]";

    public List<LinearMatchResult> findAllLinks(WikipediaPage page) {
        final List<LinearMatchResult> matches = new ArrayList<>(100);
        // Each link found may contain nested links which are added after
        int start = 0;
        while (start >= 0 && start < page.getContent().length()) {
            // Use a LinkedList as some elements will be prepended
            final List<LinearMatchResult> subMatches = new LinkedList<>();
            start = findLink(page, start, subMatches);
            matches.addAll(subMatches);
        }
        return matches;
    }

    private int findLink(WikipediaPage page, int start, List<LinearMatchResult> matches) {
        final String text = page.getContent();
        final int startLink = findStartLink(text, start);
        if (startLink >= 0) {
            final LinearMatchResult completeMatch = findNestedLink(text, startLink, matches);
            if (completeMatch == null) {
                // Link not closed. Not worth keep on searching as the next links are considered as nested.
                FinderUtils.logFinderResult(page, startLink, startLink + START_LINK.length(), "Link not closed");
                return -1;
            } else {
                matches.add(0, completeMatch);
                return completeMatch.end();
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

    /* Find the immutable of the link. It also finds nested links and adds them to the given list. */
    @Nullable
    private LinearMatchResult findNestedLink(String text, int startLink, List<LinearMatchResult> matches) {
        int start = startLink;
        while (true) {
            if (text.startsWith(START_LINK, start)) {
                start += START_LINK.length();
            }
            final int end = findEndLink(text, start);
            if (end < 0) {
                return null;
            }

            final int startNested = findStartLink(text, start);
            if (startNested >= 0 && startNested < end) {
                // Nested
                // Find the end of the nested which can be the found end or forward in case of more nesting levels
                final LinearMatchResult nestedMatch = findNestedLink(text, startNested, matches);
                if (nestedMatch == null) {
                    return null;
                }

                matches.add(0, nestedMatch);

                // Prepare to find the next nested
                start = nestedMatch.end();
            } else {
                return LinearMatchResult.of(startLink, text.substring(startLink, end + END_LINK.length()));
            }
        }
    }
}
