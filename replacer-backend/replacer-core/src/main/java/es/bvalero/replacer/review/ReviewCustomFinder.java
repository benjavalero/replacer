package es.bvalero.replacer.review;

import es.bvalero.replacer.common.domain.PageKey;
import es.bvalero.replacer.common.domain.PageTitle;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.CustomReplacementFindApi;
import es.bvalero.replacer.finder.CustomType;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.index.PageIndexApi;
import es.bvalero.replacer.page.PageRepository;
import es.bvalero.replacer.page.PageSaveRepository;
import es.bvalero.replacer.replacement.CustomRepository;
import es.bvalero.replacer.wikipedia.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.TestOnly;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Qualifier("reviewCustomFinder")
@Component
class ReviewCustomFinder extends ReviewFinder {

    // Dependency injection
    private final WikipediaPageRepository wikipediaPageRepository;
    private final CustomRepository customRepository;
    private final CustomReplacementFindApi customReplacementFindApi;

    @Setter(onMethod_ = @TestOnly)
    @Value("${replacer.indexable.namespaces}")
    private Set<Integer> indexableNamespaces;

    public ReviewCustomFinder(
        WikipediaPageRepository wikipediaPageRepository,
        @Qualifier("pageIndexService") PageIndexApi pageIndexApi,
        PageRepository pageRepository,
        PageSaveRepository pageSaveRepository,
        ReviewSectionFinder reviewSectionFinder,
        CustomRepository customRepository,
        CustomReplacementFindApi customReplacementFindApi
    ) {
        super(wikipediaPageRepository, pageIndexApi, pageRepository, pageSaveRepository, reviewSectionFinder);
        this.wikipediaPageRepository = wikipediaPageRepository;
        this.customRepository = customRepository;
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
        final AtomicInteger totalToReview = new AtomicInteger(searchResult.getTotal());
        final Set<Integer> pageIds = new HashSet<>(searchResult.getPageIds());

        // Find all the pages already reviewed for this custom replacement
        // Make it out of the loop as we are not taking into account the offset for this
        Collection<Integer> reviewedIds = searchResult.isEmpty()
            ? Collections.emptyList()
            : findReviewedPageIds(options);

        while (totalToReview.get() >= 0) {
            // Discard the pages already reviewed
            // For the moment we don't check the positions of the replacements,
            // which means that once a custom replacement is reviewed for a page it is done forever.
            for (Integer reviewId : reviewedIds) {
                if (pageIds.remove(reviewId)) {
                    totalToReview.decrementAndGet();
                }
            }
            LOGGER.debug("After discarding reviewed: {}", totalToReview);

            // Also discard the pages not containing the custom replacement according to the rules of the tool.
            // For that, we retrieve the potential custom replacements in the pages found by the Wikipedia search.
            try {
                findWikipediaPagesById(lang, pageIds).forEach(page -> {
                    final Collection<Replacement> customReplacements = findCustomReplacements(page, options);
                    if (customReplacements.isEmpty()) {
                        pageIds.remove(page.getPageKey().getPageId());
                        totalToReview.decrementAndGet();
                    }
                });
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
                return PageSearchResult.of(totalToReview.get(), pageKeys, nextOffset);
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
        return wikipediaPageRepository.findByContent(searchRequest, options.getUser().getAccessToken());
    }

    private Collection<Integer> findReviewedPageIds(ReviewOptions options) {
        WikipediaLanguage lang = options.getUser().getId().getLang();
        return customRepository
            .findPagesReviewed(lang, options.getCustomType())
            .stream()
            .map(PageKey::getPageId)
            .toList();
    }

    private Stream<WikipediaPage> findWikipediaPagesById(WikipediaLanguage lang, Collection<Integer> pageIds)
        throws WikipediaException {
        final List<PageKey> keys = pageIds.stream().map(id -> PageKey.of(lang, id)).toList();
        return wikipediaPageRepository.findByKeys(keys);
    }

    private Collection<Replacement> findCustomReplacements(WikipediaPage page, ReviewOptions options) {
        return customReplacementFindApi.findCustomReplacements(
            FinderPage.of(page),
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
        SortedSet<Replacement> merged = Stream.of(customReplacements, replacements)
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

    @Override
    public Collection<PageTitle> findPageTitlesToReviewByType(ReviewOptions options) {
        // We use the same approach that for finding a review,
        // i.e. search all the results and then discard the ones not to review.
        try {
            final WikipediaLanguage lang = options.getUser().getId().getLang();
            final WikipediaSearchResult searchResult = findWikipediaResults(options, 0);
            final Set<Integer> pageIds = new HashSet<>(searchResult.getPageIds());
            findReviewedPageIds(options).forEach(pageIds::remove);
            return findWikipediaPagesById(lang, pageIds)
                .filter(page -> !findCustomReplacements(page, options).isEmpty())
                .map(page -> PageTitle.of(page.getPageKey().getPageId(), page.getTitle()))
                .toList();
        } catch (WikipediaException e) {
            LOGGER.error("ERROR Find Page Titles to review by type", e);
            return List.of();
        }
    }
}
