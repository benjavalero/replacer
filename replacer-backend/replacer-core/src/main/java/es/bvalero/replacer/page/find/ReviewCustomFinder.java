package es.bvalero.replacer.page.find;

import es.bvalero.replacer.common.domain.PageKey;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.CustomReplacementFindApi;
import es.bvalero.replacer.finder.CustomType;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.page.index.PageIndexService;
import es.bvalero.replacer.page.save.PageSaveRepository;
import es.bvalero.replacer.replacement.CustomReplacementService;
import es.bvalero.replacer.wikipedia.WikipediaException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.TestOnly;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
class ReviewCustomFinder extends ReviewFinder {

    // Dependency injection
    private final WikipediaPageRepository wikipediaPageRepository;
    private final CustomReplacementService customReplacementService;
    private final CustomReplacementFindApi customReplacementFindApi;

    @Setter(onMethod_ = @TestOnly)
    @Value("${replacer.indexable.namespaces}")
    private Set<Integer> indexableNamespaces;

    public ReviewCustomFinder(
        WikipediaPageRepository wikipediaPageRepository,
        PageIndexService pageIndexService,
        PageRepository pageRepository,
        PageSaveRepository pageSaveRepository,
        ReviewSectionFinder reviewSectionFinder,
        CustomReplacementService customReplacementService,
        CustomReplacementFindApi customReplacementFindApi
    ) {
        super(wikipediaPageRepository, pageIndexService, pageRepository, pageSaveRepository, reviewSectionFinder);
        this.wikipediaPageRepository = wikipediaPageRepository;
        this.customReplacementService = customReplacementService;
        this.customReplacementFindApi = customReplacementFindApi;
    }

    @Override
    boolean reloadIfCacheIsEmpty() {
        return false;
    }

    @Override
    PageSearchResult findPageIdsToReview(ReviewOptions options) {
        // Initialize search
        final WikipediaLanguage lang = options.getUser().getId().getLang();
        int offset = findCachedResult(options).map(PageSearchResult::getOffset).orElse(0);

        final WikipediaSearchResult searchResult = findWikipediaResults(options, offset);
        final int totalWikipediaResults = searchResult.getTotal();
        int totalToReview = searchResult.getTotal();
        final Set<Integer> pageIds = new HashSet<>(searchResult.getPageIds());

        // Find all the pages already reviewed for this custom replacement
        // Make it out of the loop as we are not taking into account the offset for this
        Collection<Integer> reviewedIds = searchResult.isEmpty()
            ? Collections.emptyList()
            : customReplacementService.findPagesReviewed(lang, options.getCustomType());

        while (totalToReview >= 0) {
            // Discard the pages already reviewed
            // For the moment we don't check the positions of the replacements,
            // which means that once a custom replacement is reviewed for a page it is done forever.
            for (Integer reviewId : reviewedIds) {
                if (pageIds.remove(reviewId)) {
                    totalToReview--;
                }
            }
            LOGGER.debug("After discarding reviewed: {}", totalToReview);

            // Also discard the pages not containing the custom replacement according to the rules of the tool.
            // For that, we retrieve the potential custom replacements in the pages found by the Wikipedia search.
            try {
                final List<PageKey> keys = pageIds.stream().map(id -> PageKey.of(lang, id)).toList();
                final Collection<WikipediaPage> pages = wikipediaPageRepository.findByKeys(keys);
                for (WikipediaPage page : pages) {
                    final Collection<Replacement> customReplacements = findCustomReplacements(page, options);
                    if (customReplacements.isEmpty()) {
                        pageIds.remove(page.getPageId());
                        totalToReview--;
                    }
                }
            } catch (WikipediaException e) {
                // Do nothing, simply we don't discard in case we cannot retrieve the pages.
            }
            LOGGER.debug("After discarding without replacements: {}", totalToReview);

            if (pageIds.isEmpty()) {
                offset += getCacheSize();
                if (offset >= totalWikipediaResults) {
                    LOGGER.debug("All results retrieved from Wikipedia are already reviewed");
                    return PageSearchResult.ofEmpty();
                }

                // Find the next batch of results
                // For simplicity's sake we assume the number of total results is the same
                pageIds.addAll(findWikipediaResults(options, offset).getPageIds());
            } else {
                int nextOffset = offset + getCacheSize();
                Collection<PageKey> pageKeys = pageIds
                    .stream()
                    .map(pageId -> PageKey.of(lang, pageId))
                    .collect(Collectors.toUnmodifiableSet());
                // Note that the total returned is an estimation, but it is not worth to check all the results.
                // E.g. The initial search returns 1000 results. We check the first 500 and discard 498.
                // This method will return 2 pages and a total of 502. Maybe the rest of pages can be discarded or not.
                return PageSearchResult.of(totalToReview, pageKeys, nextOffset);
            }
        }

        return PageSearchResult.ofEmpty();
    }

    private WikipediaSearchResult findWikipediaResults(ReviewOptions options, int offset) {
        WikipediaLanguage lang = options.getUser().getId().getLang();
        CustomType customType = options.getCustomType();
        WikipediaSearchRequest searchRequest = WikipediaSearchRequest.builder()
            .lang(lang)
            .namespaces(
                this.indexableNamespaces.stream()
                    .map(WikipediaNamespace::valueOf)
                    .collect(Collectors.toUnmodifiableSet())
            )
            .text(customType.getSubtype())
            .caseSensitive(customType.isCaseSensitive())
            .offset(offset)
            .limit(getCacheSize())
            .build();
        return wikipediaPageRepository.findByContent(searchRequest);
    }

    private Collection<Replacement> findCustomReplacements(WikipediaPage page, ReviewOptions options) {
        return customReplacementFindApi.findCustomReplacements(
            page.toFinderPage(),
            options.getCustomReplacementFindRequest()
        );
    }

    @Override
    Collection<Replacement> decorateReplacements(
        WikipediaPage page,
        ReviewOptions options,
        Collection<Replacement> replacements
    ) {
        Collection<Replacement> customReplacements = findCustomReplacements(page, options);

        // Add the custom replacements to the standard ones preferring the custom ones
        // Return the merged collection as a TreeSet to keep the order and discard duplicates
        // We also check there are no replacements containing others
        Collection<Replacement> merged = Stream.of(customReplacements, replacements)
            .flatMap(Collection::stream)
            .collect(Collectors.toCollection(TreeSet::new));
        Replacement.removeNested(merged);

        // We run a filter to check there is at least one replacement of the requested type
        Collection<Replacement> filtered = filterReplacementsByType(merged, options.getCustomType());
        if (filtered.isEmpty()) {
            return List.of();
        }

        return merged;
    }
}
