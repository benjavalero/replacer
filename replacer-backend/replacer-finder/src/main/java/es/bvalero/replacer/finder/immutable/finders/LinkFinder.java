package es.bvalero.replacer.finder.immutable.finders;

import static es.bvalero.replacer.finder.util.FinderUtils.*;

import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.util.ReplacerUtils;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.FinderPriority;
import es.bvalero.replacer.finder.immutable.ImmutableCheckedFinder;
import es.bvalero.replacer.finder.util.FinderMatchResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
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

    private static final char COLON = ':';

    // Dependency injection
    private final FinderProperties finderProperties;
    private final UppercaseFinder uppercaseFinder;

    private final Set<String> categoryWords = new HashSet<>();
    private final Set<String> fileSpaces = new HashSet<>();

    LinkFinder(FinderProperties finderProperties, UppercaseFinder uppercaseFinder) {
        this.finderProperties = finderProperties;
        this.uppercaseFinder = uppercaseFinder;
    }

    @PostConstruct
    public void init() {
        this.categoryWords.addAll(this.finderProperties.getAllCategoryWords()); // To avoid calculating
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
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        final List<MatchResult> immutables = new ArrayList<>(100);
        for (MatchResult template : FinderUtils.findAllStructures(page, START_LINK, END_LINK)) {
            immutables.addAll(findImmutable(template, page));
        }
        return immutables.stream();
    }

    private List<MatchResult> findImmutable(MatchResult link, FinderPage page) {
        final List<MatchResult> immutables = new ArrayList<>();

        // Let's check first the easiest cases
        final WikipediaLanguage lang = page.getPageKey().getLang();
        final String text = page.getContent();

        // If the link is suffixed then return the complete link
        final int endSuffix = findEndSuffix(text, link.end());
        if (endSuffix > link.end()) {
            return List.of(FinderMatchResult.of(text, link.start(), endSuffix));
        }

        final String linkContent = getLinkContent(link.group());
        final String linkTitle = findLinkTitle(linkContent);
        final String linkSpace = findLinkSpace(linkTitle);

        // If the link space is a category then return an immutable of the complete link
        if (isCategorySpace(linkSpace)) {
            return List.of(link);
        }

        // NOTE: the positions of this result are relative to the link content not to the whole link
        final MatchResult linkAlias = findLinkAlias(linkContent);

        // Interwiki links are immutable. If alias exists, then return only the link title.
        // The same applies for interlanguage links and files
        // Exception: if the alias exists but is a parameter then return the complete link
        if (linkAlias != null || isInterWikiSpace(linkSpace) || isLangSpace(linkSpace) || isFileSpace(linkSpace)) {
            if (linkAlias == null) {
                return List.of(link);
            } else if (isAliasParameter(linkAlias.group())) {
                return List.of(link);
            } else {
                final int startTitle = link.start() + START_LINK.length();
                immutables.add(FinderMatchResult.of(startTitle, linkTitle));

                // If the alias starts with an uppercase word/expression then we return this word
                final Optional<MatchResult> firstExpressionUpperCase = uppercaseFinder.findFirstExpressionUpperCase(
                    linkAlias.group(),
                    lang
                );
                firstExpressionUpperCase.ifPresent(uppercase -> {
                    final int startAlias = startTitle + linkAlias.start();
                    immutables.add(FinderMatchResult.of(startAlias + uppercase.start(), uppercase.group()));
                });
            }
        }

        return immutables;
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

    private String getLinkContent(String link) {
        return link.substring(START_LINK.length(), link.length() - END_LINK.length());
    }

    private String findLinkTitle(String linkContent) {
        final int startLastPipe = linkContent.lastIndexOf(PIPE);
        return startLastPipe >= 0 ? linkContent.substring(0, startLastPipe) : linkContent;
    }

    @Nullable
    private String findLinkSpace(String title) {
        final int startColon = title.indexOf(COLON);
        return startColon >= 0 ? title.substring(0, startColon) : null;
    }

    private boolean isCategorySpace(@Nullable String space) {
        return (space != null && this.categoryWords.contains(ReplacerUtils.setFirstUpperCase(space.trim())));
    }

    @Nullable
    private MatchResult findLinkAlias(String linkContent) {
        final int startLastPipe = linkContent.lastIndexOf(PIPE);
        return startLastPipe >= 0 ? FinderMatchResult.of(linkContent, startLastPipe + 1, linkContent.length()) : null;
    }

    private boolean isFileSpace(@Nullable String space) {
        return space != null && this.fileSpaces.contains(ReplacerUtils.setFirstUpperCase(space.trim()));
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
