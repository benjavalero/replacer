package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.finder.*;
import es.bvalero.replacer.page.IndexablePage;
import java.util.*;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find link-related immutables:
 * <ul>
 *     <li>Find categories, e.g. `[[Categoría:España]]`</li>
 *     <li>Find links with suffix, e.g. `[[brasil]]eño`</li>
 *     <li>Find the first part of aliased links, e.g. `brasil` in `[[brasil|Brasil]]`. It also finds files.</li>
 *     <li>Find inter-language links, e.g. `[[pt:Title]]`</li>
 *     <li>Find filenames, e.g. `xx.jpg` in `[[File:xx.jpg]]`</li>
 * </ul>
 */
@Slf4j
@Component
public class LinkFinder implements ImmutableFinder {

    private static final String START_LINK = "[[";
    private static final String END_LINK = "]]";

    private static final Set<String> completeSpaces = Set.of("categoría");

    @Resource
    private Set<String> fileSpaces;

    @Override
    public ImmutableFinderPriority getPriority() {
        return ImmutableFinderPriority.LOW;
    }

    @Override
    public int getMaxLength() {
        return 250;
    }

    @Override
    public Iterable<Immutable> find(IndexablePage page) {
        List<Immutable> immutables = new ArrayList<>(100);
        for (LinearMatcher template : findAllLinks(page)) {
            immutables.addAll(findImmutables(template, page.getContent()));
        }
        return immutables;
    }

    List<LinearMatcher> findAllLinks(IndexablePage page) {
        List<LinearMatcher> matches = new ArrayList<>(100);
        // Each link found may contain nested links which are added after
        int start = 0;
        while (start >= 0 && start < page.getContent().length()) {
            List<LinearMatcher> subMatches = new LinkedList<>();
            start = findLink(page, start, subMatches);
            matches.addAll(subMatches);
        }
        return matches;
    }

    private int findLink(IndexablePage page, int start, List<LinearMatcher> matches) {
        String text = page.getContent();
        int startLink = findStartLink(text, start);
        if (startLink >= 0) {
            LinearMatcher completeMatch = findNestedLink(text, startLink, matches);
            if (completeMatch != null) {
                matches.add(0, completeMatch);
                return completeMatch.end();
            } else {
                // Link not closed. Not worth keep on searching.
                Immutable immutable = Immutable.of(
                    startLink,
                    FinderUtils.getContextAroundWord(text, startLink, startLink, getContextThreshold()),
                    this
                );
                logWarning(immutable, page, LOGGER, "Link not closed");
                return -1;
            }
        } else {
            // No more link
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
    private LinearMatcher findNestedLink(String text, int startLink, List<LinearMatcher> matches) {
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
                LinearMatcher nestedMatch = findNestedLink(text, startNested, matches);
                if (nestedMatch == null) {
                    return null;
                }

                matches.add(0, nestedMatch);

                // Prepare to find the next nested
                start = nestedMatch.end();
            } else {
                return LinearMatcher.of(startLink, text.substring(startLink, end + END_LINK.length()));
            }
        }
    }

    private List<Immutable> findImmutables(LinearMatcher link, String text) {
        // If the link is suffixed then return the complete link
        int endSuffix = findEndSuffix(text, link.end());
        if (endSuffix > link.end()) {
            return Collections.singletonList(Immutable.of(link.start(), text.substring(link.start(), endSuffix), this));
        }

        String content = link.group().substring(START_LINK.length(), link.group().length() - END_LINK.length());

        int posPipe = content.indexOf('|');
        String linkTitle = posPipe >= 0 ? content.substring(0, posPipe) : content;
        String linkAlias = posPipe >= 0 ? content.substring(posPipe + 1) : null;

        int posColon = linkTitle.indexOf(':');
        String linkSpace = posColon >= 0 ? linkTitle.substring(0, posColon) : null;

        // If the link space is in the list then return an immutable of the complete link
        if (isCompleteSpace(linkSpace)) {
            return Collections.singletonList(this.convert(link));
        }

        // If the link alias doesn't exist and the link is in file/lang space then return the complete link
        if (linkAlias == null && (isFileSpace(linkSpace) || isLangSpace(linkSpace) || isInterWikiSpace(linkSpace))) {
            return Collections.singletonList(this.convert(link));
        }

        // If the link alias exists then return the link title
        if (linkAlias != null) {
            return Collections.singletonList(Immutable.of(link.start() + START_LINK.length(), linkTitle, this));
        }

        // In any other case then return no immutable
        return Collections.emptyList();
    }

    private int findEndSuffix(String text, int start) {
        for (int i = start; i < text.length(); i++) {
            if (!Character.isLowerCase(text.charAt(i))) {
                return i;
            }
        }
        // In case we reach the end of the text
        return text.length();
    }

    private boolean isCompleteSpace(@Nullable String space) {
        return space != null && completeSpaces.contains(FinderUtils.toLowerCase(space).trim());
    }

    private boolean isFileSpace(@Nullable String space) {
        return space != null && fileSpaces.contains(FinderUtils.toLowerCase(space).trim());
    }

    private boolean isLangSpace(@Nullable String space) {
        return space != null && space.trim().length() == 2 && FinderUtils.isAsciiLowercase(space.trim());
    }

    private boolean isInterWikiSpace(@Nullable String space) {
        return space != null && space.trim().length() == 0;
    }
}
