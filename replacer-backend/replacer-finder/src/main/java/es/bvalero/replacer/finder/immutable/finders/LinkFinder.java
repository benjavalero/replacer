package es.bvalero.replacer.finder.immutable.finders;

import static es.bvalero.replacer.finder.util.LinkUtils.END_LINK;
import static es.bvalero.replacer.finder.util.LinkUtils.START_LINK;

import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.FinderPriority;
import es.bvalero.replacer.finder.immutable.ImmutableCheckedFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import es.bvalero.replacer.finder.util.LinkUtils;
import java.util.*;
import java.util.regex.MatchResult;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find link-related immutables:
 * <ul>
 *     <li>Categories, e.g. `[[Categoría:España]]`</li>
 *     <li>Links with suffix, e.g. `[[brasil]]eño`</li>
 *     <li>The first part of aliased links, e.g. `brasil` in `[[brasil|Brasil]]`. It also finds files.</li>
 *     <li>Inter-language links, e.g. `[[pt:Title]]`</li>
 *     <li>Filenames, e.g. `xx.jpg` in `[[File:xx.jpg]]`</li>
 * </ul>
 */
@Component
class LinkFinder extends ImmutableCheckedFinder {

    private static final char PIPE = '|';
    private static final char COLON = ':';

    @Autowired
    private FinderProperties finderProperties;

    private final Set<String> fileSpaces = new HashSet<>();

    @PostConstruct
    public void init() {
        this.fileSpaces.addAll(this.finderProperties.getAllFileWords());
        this.fileSpaces.addAll(this.finderProperties.getAllImageWords());
    }

    @Override
    public FinderPriority getPriority() {
        return FinderPriority.LOW;
    }

    @Override
    public int getMaxLength() {
        return 250;
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        final List<MatchResult> immutables = new ArrayList<>(100);
        for (LinearMatchResult template : LinkUtils.findAllLinks(page)) {
            immutables.addAll(findImmutables(template, page));
        }
        return immutables;
    }

    private List<MatchResult> findImmutables(LinearMatchResult link, FinderPage page) {
        // If the link is suffixed then return the complete link
        final String text = page.getContent();
        final int endSuffix = findEndSuffix(text, link.end());
        if (endSuffix > link.end()) {
            return Collections.singletonList(
                LinearMatchResult.of(link.start(), text.substring(link.start(), endSuffix))
            );
        }

        final String linkContent = link
            .group()
            .substring(START_LINK.length(), link.group().length() - END_LINK.length());

        // Link title and alias (optional) depend on the link pipe
        final int posLastPipe = linkContent.lastIndexOf(PIPE);
        final String linkTitle = posLastPipe >= 0 ? linkContent.substring(0, posLastPipe) : linkContent;
        final String linkAlias = posLastPipe >= 0 ? linkContent.substring(posLastPipe + 1) : null;

        // There could be a link "wikispace" e.g. for interwiki links or categories
        final int posColon = linkTitle.indexOf(COLON);
        final String linkSpace = posColon >= 0 ? linkTitle.substring(0, posColon) : null;

        // If the link space is a category then return an immutable of the complete link
        if (isCategorySpace(linkSpace)) {
            return Collections.singletonList(link);
        }

        // If the link alias doesn't exist and the link is in file/lang space then return the complete link
        if (linkAlias == null && (isFileSpace(linkSpace) || isLangSpace(linkSpace) || isInterWikiSpace(linkSpace))) {
            return Collections.singletonList(link);
        }

        // If the link alias exists then return the link title
        // In case the alias exists but is a parameter then return the complete link
        if (linkAlias != null) {
            if (isAliasParameter(linkAlias)) {
                return Collections.singletonList(link);
            } else {
                return Collections.singletonList(LinearMatchResult.of(link.start() + START_LINK.length(), linkTitle));
            }
        }

        // In any other case then return no immutable
        return Collections.emptyList();
    }

    // Find the end of the link suffix, i.e. lowercase chars appended to the link
    // In case of a usual link followed by a space the suffix end will be the link end
    private int findEndSuffix(String text, int start) {
        for (int i = start; i < text.length(); i++) {
            if (!Character.isLowerCase(text.charAt(i))) {
                return i;
            }
        }
        // In case we reach the end of the text
        return text.length();
    }

    private boolean isCategorySpace(@Nullable String space) {
        return (
            space != null &&
            this.finderProperties.getAllCategoryWords().contains(FinderUtils.setFirstUpperCase(space.trim()))
        );
    }

    private boolean isFileSpace(@Nullable String space) {
        return space != null && this.fileSpaces.contains(FinderUtils.setFirstUpperCase(space.trim()));
    }

    private boolean isLangSpace(@Nullable String space) {
        if (space == null) {
            return false;
        } else {
            final String trimmed = space.trim();
            return trimmed.length() == 2 && FinderUtils.isAsciiLowerCase(trimmed);
        }
    }

    private boolean isInterWikiSpace(@Nullable String space) {
        return space != null && StringUtils.isBlank(space);
    }

    private boolean isAliasParameter(String alias) {
        return alias.contains("=");
    }
}
