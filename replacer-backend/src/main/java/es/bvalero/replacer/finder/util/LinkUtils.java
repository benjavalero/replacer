package es.bvalero.replacer.finder.util;

import es.bvalero.replacer.finder.FinderPage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LinkUtils {

    public static final String START_LINK = "[[";
    public static final String END_LINK = "]]";

    public static List<LinearMatchResult> findAllLinks(FinderPage page) {
        List<LinearMatchResult> matches = new ArrayList<>(100);
        // Each link found may contain nested links which are added after
        int start = 0;
        while (start >= 0 && start < page.getContent().length()) {
            // Use a LinkedList as some elements will be prepended
            List<LinearMatchResult> subMatches = new LinkedList<>();
            start = findLink(page, start, subMatches);
            matches.addAll(subMatches);
        }
        return matches;
    }

    private static int findLink(FinderPage page, int start, List<LinearMatchResult> matches) {
        String text = page.getContent();
        int startLink = findStartLink(text, start);
        if (startLink >= 0) {
            LinearMatchResult completeMatch = findNestedLink(text, startLink, matches);
            if (completeMatch == null) {
                // Link not closed. Not worth keep on searching as the next links are considered as nested.
                FinderUtils.logWarning(text, startLink, startLink + START_LINK.length(), page, "Link not closed");
                return -1;
            } else {
                matches.add(0, completeMatch);
                return completeMatch.end();
            }
        } else {
            return -1;
        }
    }

    private static int findStartLink(String text, int start) {
        return text.indexOf(START_LINK, start);
    }

    private static int findEndLink(String text, int start) {
        return text.indexOf(END_LINK, start);
    }

    /* Find the immutable of the link. It also finds nested links and adds them to the given list. */
    @Nullable
    private static LinearMatchResult findNestedLink(String text, int startLink, List<LinearMatchResult> matches) {
        int start = startLink;
        while (true) {
            if (text.startsWith(START_LINK, start)) {
                start += START_LINK.length();
            }
            int end = findEndLink(text, start);
            if (end < 0) {
                return null;
            }

            int startNested = findStartLink(text, start);
            if (startNested >= 0 && startNested < end) {
                // Nested
                // Find the end of the nested which can be the found end or forward in case of more nesting levels
                LinearMatchResult nestedMatch = findNestedLink(text, startNested, matches);
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
