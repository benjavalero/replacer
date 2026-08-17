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
            final SequencedCollection<LinkStructureOld> links = LinkStructureFinderOld.findNext(page, start);
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

    private List<MatchResult> findImmutable(LinkStructureOld link, FinderPage page) {
        final String text = page.getContent();

        final int endSuffix = findSuffixEnd(text, link.end());
        if (endSuffix > link.end()) {
            return List.of(FinderMatchRange.of(text, link.start(), endSuffix));
        }

        final String linkSpace = findLinkSpace(text, link);
        final String linkSpaceNormalized = normalizeLinkSpace(linkSpace);

        if (isCategorySpace(linkSpaceNormalized)) {
            return List.of(FinderMatchRange.of(text, link.start(), link.end()));
        }

        final int linkContentStart = link.start() + START_LINK.length();
        final int linkContentEnd = link.end() - END_LINK.length();
        final int pipePos = link.pipePos();
        final int startAlias = pipePos >= linkContentStart && pipePos < linkContentEnd ? pipePos + 1 : -1;
        final boolean hasAlias = startAlias >= 0;

        if (hasAlias) {
            if (isAliasParameter(text, startAlias, linkContentEnd)) {
                return List.of(FinderMatchRange.of(text, link.start(), link.end()));
            }

            final List<MatchResult> immutables = new ArrayList<>();
            immutables.add(FinderMatchRange.of(text, linkContentStart, pipePos));
            // If the value starts with an uppercase word/expression then we return this word
            final MatchResult uppercase = uppercaseFinder.findFirstExpressionUpperCase(page, startAlias);
            if (uppercase != null) {
                immutables.add(uppercase);
            }
            return immutables;
        } else {
            final boolean isSpace =
                isInterWikiSpace(linkSpace) || isLangSpace(linkSpace) || isFileSpace(linkSpaceNormalized);
            if (isSpace) {
                return List.of(FinderMatchRange.of(text, link.start(), link.end()));
            }
        }

        return Collections.emptyList();
    }

    private int findSuffixEnd(String text, int start) {
        int end = start;
        while (end < text.length() && Character.isLowerCase(text.charAt(end))) {
            end++;
        }
        return end;
    }

    @Nullable
    private String findLinkSpace(String text, LinkStructureOld link) {
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

    private boolean isAliasParameter(String text, int startAlias, int endAlias) {
        return text.indexOf("=", startAlias, endAlias) >= 0;
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
}
