package es.bvalero.replacer.finder.immutable.finders;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.immutable.Immutable;
import es.bvalero.replacer.finder.immutable.ImmutableCheckedFinder;
import es.bvalero.replacer.finder.immutable.ImmutableFinderPriority;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import es.bvalero.replacer.finder.util.LinkUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import javax.annotation.Resource;
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

    @Resource
    private Set<String> fileSpaces;

    @Resource
    private Set<String> completeSpaces;

    @Override
    public ImmutableFinderPriority getPriority() {
        return ImmutableFinderPriority.LOW;
    }

    @Override
    public int getMaxLength() {
        return 250;
    }

    @Override
    public Iterable<Immutable> find(FinderPage page) {
        List<Immutable> immutables = new ArrayList<>(100);
        for (LinearMatchResult template : LinkUtils.findAllLinks(page)) {
            immutables.addAll(findImmutables(template, page));
        }
        return immutables;
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        // We are overriding the more general find method
        throw new IllegalCallerException();
    }

    private List<Immutable> findImmutables(LinearMatchResult link, FinderPage page) {
        // If the link is suffixed then return the complete link
        String text = page.getContent();
        int endSuffix = findEndSuffix(text, link.end());
        if (endSuffix > link.end()) {
            return Collections.singletonList(Immutable.of(link.start(), text.substring(link.start(), endSuffix)));
        }

        String content = link.group().substring(START_LINK.length(), link.group().length() - END_LINK.length());

        int posPipe = content.lastIndexOf('|');
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
        // In case the alias exists but is a parameter then return the complete link
        if (linkAlias != null) {
            if (linkAlias.contains("=")) {
                return Collections.singletonList(this.convert(link));
            } else {
                return Collections.singletonList(Immutable.of(link.start() + START_LINK.length(), linkTitle));
            }
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
