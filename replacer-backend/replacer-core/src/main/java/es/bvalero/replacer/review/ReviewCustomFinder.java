package es.bvalero.replacer.review;

import es.bvalero.replacer.finder.CustomReplacementFindService;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.replacement.CustomReplacementService;
import es.bvalero.replacer.wikipedia.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.TestOnly;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
class ReviewCustomFinder extends ReviewFinder {

    @Autowired
    private CustomReplacementService customReplacementService;

    @Autowired
    private WikipediaPageRepository wikipediaPageRepository;

    @Autowired
    private CustomReplacementFindService customReplacementFindService;

    @Setter(onMethod_ = @TestOnly)
    @Value("${replacer.indexable.namespaces}")
    private Set<Integer> indexableNamespaces;

    @Override
    PageSearchResult findPageIdsToReview(ReviewOptions options) {
        // Initialize search
        int offset = findCachedResult(options).map(PageSearchResult::getOffset).orElse(0);

        WikipediaSearchResult searchResult = findWikipediaResults(options, offset);
        final int totalWikipediaResults = searchResult.getTotal();
        int totalToReview = searchResult.getTotal();
        final Set<Integer> pageIds = new HashSet<>(searchResult.getPageIds());

        // Find all the pages already reviewed for this custom replacement
        // Make it out of the loop as we are not taking into account the offset for this
        List<Integer> reviewedIds = new ArrayList<>();
        if (!pageIds.isEmpty()) {
            reviewedIds.addAll(
                customReplacementService.findPagesReviewed(
                    options.getUserId().getLang(),
                    options.getType().toCustomType()
                )
            );
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
                    .map(pageId -> PageKey.of(options.getUserId().getLang(), pageId))
                    .collect(Collectors.toUnmodifiableSet());
                return PageSearchResult.of(totalToReview, pageKeys, nextOffset);
            }
        }

        return PageSearchResult.ofEmpty();
    }

    private WikipediaSearchResult findWikipediaResults(ReviewOptions options, int offset) {
        WikipediaSearchRequest searchRequest = WikipediaSearchRequest
            .builder()
            .lang(options.getUserId().getLang())
            .namespaces(
                this.indexableNamespaces.stream()
                    .map(WikipediaNamespace::valueOf)
                    .collect(Collectors.toUnmodifiableSet())
            )
            .text(options.getType().getSubtype())
            .caseSensitive(options.getType().toCustomType().isCaseSensitive())
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
            options.getType().toCustomType()
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
        Collection<Replacement> filtered = filterReplacementsByType(merged, options);
        if (filtered.isEmpty()) {
            return Collections.emptyList();
        }

        return merged;
    }
}
