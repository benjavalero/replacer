package es.bvalero.replacer.finder.immutable;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.util.*;
import java.util.regex.MatchResult;
import javax.annotation.Resource;
import org.jetbrains.annotations.VisibleForTesting;
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
@Component
class LinkFinder extends ImmutableCheckedFinder {

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
    int getMaxLength() {
        return 250;
    }

    @Override
    public Iterable<Immutable> find(FinderPage page) {
        List<Immutable> immutables = new ArrayList<>(100);
        for (LinearMatchResult template : findAllLinks(page)) {
            immutables.addAll(findImmutables(template, page));
        }
        return immutables;
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        // We are overriding the more general find method
        throw new IllegalCallerException();
    }

    @VisibleForTesting
    List<LinearMatchResult> findAllLinks(FinderPage page) {
        List<LinearMatchResult> matches = new ArrayList<>(100);
        // Each link found may contain nested links which are added after
        int start = 0;
        while (start >= 0 && start < page.getContent().length()) {
            List<LinearMatchResult> subMatches = new LinkedList<>();
            start = findLink(page, start, subMatches);
            matches.addAll(subMatches);
        }
        return matches;
    }

    private int findLink(FinderPage page, int start, List<LinearMatchResult> matches) {
        String text = page.getContent();
        int startLink = findStartLink(text, start);
        if (startLink >= 0) {
            LinearMatchResult completeMatch = findNestedLink(text, startLink, matches);
            if (completeMatch != null) {
                matches.add(0, completeMatch);
                return completeMatch.end();
            } else {
                // Link not closed. Not worth keep on searching.
                Immutable immutable = Immutable.of(
                    startLink,
                    FinderUtils.getContextAroundWord(text, startLink, startLink, CONTEXT_THRESHOLD)
                );
                logWarning(immutable, page, "Link not closed");
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
    private LinearMatchResult findNestedLink(String text, int startLink, List<LinearMatchResult> matches) {
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

    private List<Immutable> findImmutables(LinearMatchResult link, FinderPage page) {
        // If the link is suffixed then return the complete link
        String text = page.getContent();
        int endSuffix = findEndSuffix(text, link.end());
        if (endSuffix > link.end()) {
            return Collections.singletonList(Immutable.of(link.start(), text.substring(link.start(), endSuffix)));
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
            return Collections.singletonList(Immutable.of(link.start() + START_LINK.length(), linkTitle));
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
