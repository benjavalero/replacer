package es.bvalero.replacer.finder.immutable.finders;

import static es.bvalero.replacer.finder.util.FinderUtils.*;

import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.common.util.ReplacerUtils;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.FinderPriority;
import es.bvalero.replacer.finder.immutable.ImmutableCheckedFinder;
import es.bvalero.replacer.finder.util.FinderMatchRange;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchCollectionFinder;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

/** Snapshot of {@link LinkFinder} as it was at the time the benchmark was introduced. */
public class LinkFinderOld extends ImmutableCheckedFinder {

    private final UppercaseFinder uppercaseFinder;
    private final Set<String> categoryWords = new HashSet<>();
    private final Set<String> fileSpaces = new HashSet<>();

    public LinkFinderOld(FinderProperties finderProperties, UppercaseFinder uppercaseFinder) {
        this.uppercaseFinder = uppercaseFinder;
        this.categoryWords.addAll(finderProperties.getAllCategoryWords());
        this.fileSpaces.addAll(finderProperties.getAllFileWords());
        this.fileSpaces.addAll(finderProperties.getAllImageWords());
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
        return LinearMatchCollectionFinder.find(page, this::findImmutables);
    }

    private SequencedCollection<MatchResult> findImmutables(FinderPage page, int start) {
        while (start >= 0 && start < page.getContent().length()) {
            final SequencedCollection<LinkStructure> links = LinkStructureFinderOld.findNext(page, start);
            if (links.isEmpty()) {
                break;
            }
            final List<MatchResult> immutables = links
                .stream()
                .flatMap(link -> findImmutable(link, page).stream())
                .toList();
            if (!immutables.isEmpty()) {
                return immutables;
            }
            start = links.getLast().end();
        }
        return Collections.emptyList();
    }

    private List<MatchResult> findImmutable(LinkStructure link, FinderPage page) {
        final List<MatchResult> immutables = new ArrayList<>();

        final String text = page.getContent();

        final int endSuffix = findSuffixEnd(text, link.end());
        if (endSuffix > link.end()) {
            return List.of(FinderMatchRange.of(text, link.start(), endSuffix));
        }

        final int linkContentStart = link.start() + START_LINK.length();
        final int linkContentEnd = link.end() - END_LINK.length();

        final String linkSpaceTrimmed = findLinkSpaceTrimmed(text, link);
        final String linkSpaceNormalized = normalizeLinkSpace(linkSpaceTrimmed);

        if (isCategorySpace(linkSpaceNormalized)) {
            return List.of(FinderMatchRange.of(text, link.start(), link.end()));
        }

        final int pipePos = link.pipePos();
        final MatchResult linkAlias = findLinkAlias(text, linkContentStart, linkContentEnd, pipePos);

        if (
            linkAlias != null ||
            isInterWikiSpace(linkSpaceTrimmed) ||
            isLangSpace(linkSpaceTrimmed) ||
            isFileSpace(linkSpaceNormalized)
        ) {
            if (linkAlias == null) {
                return List.of(FinderMatchRange.of(text, link.start(), link.end()));
            } else if (isAliasParameter(linkAlias.group())) {
                return List.of(FinderMatchRange.of(text, link.start(), link.end()));
            } else {
                final int linkTitleEnd = pipePos >= linkContentStart ? pipePos : linkContentEnd;
                immutables.add(FinderMatchRange.of(text, linkContentStart, linkTitleEnd));
                uppercaseFinder.findFirstExpressionUpperCase(page, linkAlias.start()).ifPresent(immutables::add);
            }
        }

        return immutables;
    }

    private int findSuffixEnd(String text, int start) {
        int end = start;
        while (end < text.length() && Character.isLowerCase(text.charAt(end))) {
            end++;
        }
        return end;
    }

    @Nullable
    private String findLinkSpaceTrimmed(String text, LinkStructure link) {
        final int colonPos = link.colonPos();
        if (colonPos < 0) {
            return null;
        }
        final int start = FinderUtils.trimLeft(text, link.start() + START_LINK.length(), colonPos);
        final int end = FinderUtils.trimRight(text, start, colonPos);
        return text.substring(start, end);
    }

    @Nullable
    private String normalizeLinkSpace(@Nullable String trimmedSpace) {
        return trimmedSpace != null ? ReplacerUtils.setFirstUpperCase(trimmedSpace) : null;
    }

    private boolean isCategorySpace(@Nullable String normalizedSpace) {
        return normalizedSpace != null && this.categoryWords.contains(normalizedSpace);
    }

    @Nullable
    private MatchResult findLinkAlias(String text, int start, int end, int pipePos) {
        return pipePos >= start && pipePos < end ? FinderMatchRange.of(text, pipePos + 1, end) : null;
    }

    private boolean isFileSpace(@Nullable String normalizedSpace) {
        return normalizedSpace != null && this.fileSpaces.contains(normalizedSpace);
    }

    private boolean isLangSpace(@Nullable String trimmedSpace) {
        return trimmedSpace != null && trimmedSpace.length() == 2 && FinderUtils.isAsciiLowerCase(trimmedSpace);
    }

    private boolean isInterWikiSpace(@Nullable String trimmedSpace) {
        return trimmedSpace != null && trimmedSpace.isEmpty();
    }

    private boolean isAliasParameter(String alias) {
        return alias.contains("=");
    }
}
