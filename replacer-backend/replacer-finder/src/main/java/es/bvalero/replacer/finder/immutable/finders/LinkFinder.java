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
            final List<MatchResult> immutables = new ArrayList<>();
            for (LinkStructure link : links) {
                immutables.addAll(findImmutable(link, page));
            }
            if (!immutables.isEmpty()) {
                return immutables;
            }
            // If no immutables found, search from the end of the root structure (last element)
            start = links.getLast().end();
        }
        return Collections.emptyList();
    }

    private List<MatchResult> findImmutable(LinkStructure link, FinderPage page) {
        final String text = page.getContent();

        // If the link is suffixed then return the complete link including the suffix
        // We need to calculate the suffix end position to build the immutable range
        final int endSuffix = findSuffixEnd(text, link.end());
        if (endSuffix > link.end()) {
            return List.of(FinderMatchRange.of(text, link.start(), endSuffix));
        }

        final String linkSpace = findLinkSpace(text, link);
        final String linkSpaceNormalized = normalizeLinkSpace(linkSpace);

        // If the link space is a category then return an immutable of the complete link
        if (isCategorySpace(linkSpaceNormalized)) {
            return List.of(FinderMatchRange.of(text, link.start(), link.end()));
        }

        // Interwiki links are immutable. If alias exists, then return only the link title.
        // The same applies for interlanguage links and files
        // Exception: if the alias exists but is a parameter then return the complete link
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

            // Include title
            immutables.add(FinderMatchRange.of(text, linkContentStart, pipePos));

            // Include uppercase alias if present
            final MatchResult uppercase = uppercaseFinder.findFirstExpressionUpperCase(page, startAlias);
            if (uppercase != null) {
                immutables.add(uppercase);
            }

            return immutables;
        } else {
            final boolean isSpace =
                isInterWikiSpace(linkSpace) || isLangSpace(linkSpace) || isFileSpace(linkSpaceNormalized);
            if (isSpace) {
                // Return complete link
                return List.of(FinderMatchRange.of(text, link.start(), link.end()));
            }
        }

        return Collections.emptyList();
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
    private String findLinkSpace(String text, LinkStructure link) {
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

    private boolean isFileSpace(@Nullable String normalizedSpace) {
        return normalizedSpace != null && this.fileSpaces.contains(normalizedSpace);
    }

    private boolean isLangSpace(@Nullable String trimmedSpace) {
        return trimmedSpace != null && trimmedSpace.length() == 2 && FinderUtils.isAsciiLowerCase(trimmedSpace);
    }

    private boolean isInterWikiSpace(@Nullable String trimmedSpace) {
        return trimmedSpace != null && trimmedSpace.isEmpty();
    }

    private boolean isAliasParameter(String text, int startAlias, int endAlias) {
        return text.indexOf("=", startAlias, endAlias) >= 0;
    }
}
