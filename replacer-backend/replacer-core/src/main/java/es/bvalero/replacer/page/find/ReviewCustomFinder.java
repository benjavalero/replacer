package es.bvalero.replacer.page.find;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.CustomMisspelling;
import es.bvalero.replacer.finder.CustomReplacementFindService;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.page.*;
import es.bvalero.replacer.page.index.PageIndexService;
import es.bvalero.replacer.replacement.CustomReplacementService;
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
    private final CustomReplacementService customReplacementService;
    private final WikipediaPageRepository wikipediaPageRepository;
    private final CustomReplacementFindService customReplacementFindService;

    @Setter(onMethod_ = @TestOnly)
    @Value("${replacer.indexable.namespaces}")
    private Set<Integer> indexableNamespaces;

    public ReviewCustomFinder(
        WikipediaPageRepository wikipediaPageRepository,
        PageIndexService pageIndexService,
        PageRepository pageRepository,
        ReviewSectionFinder reviewSectionFinder,
        CustomReplacementService customReplacementService,
        CustomReplacementFindService customReplacementFindService
    ) {
        super(wikipediaPageRepository, pageIndexService, pageRepository, reviewSectionFinder);
        this.customReplacementService = customReplacementService;
        this.wikipediaPageRepository = wikipediaPageRepository;
        this.customReplacementFindService = customReplacementFindService;
    }

    @Override
    boolean reloadIfCacheIsEmpty() {
        return false;
    }

    @Override
    PageSearchResult findPageIdsToReview(ReviewOptions options) {
        // Initialize search
        WikipediaLanguage lang = options.getUser().getId().getLang();
        int offset = findCachedResult(options).map(PageSearchResult::getOffset).orElse(0);

        WikipediaSearchResult searchResult = findWikipediaResults(options, offset);
        final int totalWikipediaResults = searchResult.getTotal();
        int totalToReview = searchResult.getTotal();
        final Set<Integer> pageIds = new HashSet<>(searchResult.getPageIds());

        // Find all the pages already reviewed for this custom replacement
        // Make it out of the loop as we are not taking into account the offset for this
        List<Integer> reviewedIds = new ArrayList<>();
        if (!pageIds.isEmpty()) {
            reviewedIds.addAll(customReplacementService.findPagesReviewed(lang, options.getCustomType()));
        }

        while (totalToReview >= 0) {
            // Discard the pages already reviewed
            // For the moment we don't check the positions of the replacements,
            // which means that once a custom replacement is reviewed for a page it is done forever.
            for (Integer reviewId : reviewedIds) {
                if (pageIds.remove(reviewId)) {
                    totalToReview--;
                }
            }

            if (pageIds.isEmpty()) {
                offset += getCacheSize();
                if (offset >= totalWikipediaResults) {
                    LOGGER.debug("All results retrieved from Wikipedia are already reviewed");
                    return PageSearchResult.ofEmpty();
                }

                searchResult = findWikipediaResults(options, offset);
                // For simplicity's sake we assume the number of total results is the same
                pageIds.addAll(searchResult.getPageIds());
            } else {
                int nextOffset = offset + getCacheSize();
                Collection<PageKey> pageKeys = pageIds
                    .stream()
                    .map(pageId -> PageKey.of(lang, pageId))
                    .collect(Collectors.toUnmodifiableSet());
                return PageSearchResult.of(totalToReview, pageKeys, nextOffset);
            }
        }

        return PageSearchResult.ofEmpty();
    }

    private WikipediaSearchResult findWikipediaResults(ReviewOptions options, int offset) {
        WikipediaLanguage lang = options.getUser().getId().getLang();
        CustomMisspelling customMisspelling = options.getCustomMisspelling();
        WikipediaSearchRequest searchRequest = WikipediaSearchRequest
            .builder()
            .lang(lang)
            .namespaces(
                this.indexableNamespaces.stream()
                    .map(WikipediaNamespace::valueOf)
                    .collect(Collectors.toUnmodifiableSet())
            )
            .text(customMisspelling.getWord())
            .caseSensitive(customMisspelling.isCaseSensitive())
            .offset(offset)
            .limit(getCacheSize())
            .build();
        return wikipediaPageRepository.findByContent(searchRequest);
    }

    @Override
    Collection<Replacement> decorateReplacements(
        WikipediaPage page,
        ReviewOptions options,
        Collection<Replacement> replacements
    ) {
        Collection<Replacement> customReplacements = customReplacementFindService.findCustomReplacements(
            page,
            options.getCustomMisspelling()
        );

        // Add the custom replacements to the standard ones preferring the custom ones
        // Return the merged collection as a TreeSet to keep the order and discard duplicates
        // We also check there are no replacements containing others
        Collection<Replacement> merged = Stream
            .of(customReplacements, replacements)
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
