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
import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
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
public class LinkFinder extends ImmutableCheckedFinder {

    // Dependency injection
    private final FinderProperties finderProperties;
    private final UppercaseFinder uppercaseFinder;

    private final Set<String> categoryWords = new HashSet<>();
    private final Set<String> fileSpaces = new HashSet<>();

    public LinkFinder(FinderProperties finderProperties, UppercaseFinder uppercaseFinder) {
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
        return LinearMatchCollectionFinder.find(page, this::findImmutables);
    }

    private SequencedCollection<MatchResult> findImmutables(FinderPage page, int start) {
        // findNextStructure returns nested structures first, root structure last
        while (start >= 0 && start < page.getContent().length()) {
            final SequencedCollection<LinkStructure> links = LinkStructureFinder.findNext(page, start);
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
            // If no immutables found, search from the end of the root structure (last element)
            start = links.getLast().end();
        }
        return Collections.emptyList();
    }

    private List<MatchResult> findImmutable(LinkStructure link, FinderPage page) {
        final List<MatchResult> immutables = new ArrayList<>();

        final String text = page.getContent();

        // If the link is suffixed then return the complete link including the suffix
        // We need to calculate the suffix end position to build the immutable range
        final int endSuffix = findSuffixEnd(text, link.end());
        if (endSuffix > link.end()) {
            return List.of(FinderMatchRange.of(text, link.start(), endSuffix));
        }

        final int linkContentStart = link.start() + START_LINK.length();
        final int linkContentEnd = link.end() - END_LINK.length();

        final String linkSpaceTrimmed = findLinkSpaceTrimmed(text, link);
        final String linkSpaceNormalized = normalizeLinkSpace(linkSpaceTrimmed);

        // If the link space is a category then return an immutable of the complete link
        if (isCategorySpace(linkSpaceNormalized)) {
            return List.of(FinderMatchRange.of(text, link.start(), link.end()));
        }

        // Interwiki links are immutable. If alias exists, then return only the link title.
        // The same applies for interlanguage links and files
        // Exception: if the alias exists but is a parameter then return the complete link
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
                final Optional<MatchResult> firstExpressionUpperCase = uppercaseFinder.findFirstExpressionUpperCase(
                    page,
                    linkAlias.start()
                );
                firstExpressionUpperCase.ifPresent(uppercase -> {
                    immutables.add(uppercase);
                });
            }
        }

        return immutables;
    }

    private int findSuffixEnd(String text, int start) {
        // Find the end of the lowercase suffix after ']]', e.g. '[[brasil]]eño'
        // We need the exact position to build the immutable range, so the check and the calculation are one
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
