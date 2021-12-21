package es.bvalero.replacer.page.review;

import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.page.findreplacement.FindReplacementsService;
import es.bvalero.replacer.repository.CustomRepository;
import es.bvalero.replacer.repository.PageIndexRepository;
import es.bvalero.replacer.repository.PageModel;
import es.bvalero.replacer.repository.PageRepository;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaSearchResult;
import es.bvalero.replacer.wikipedia.WikipediaService;
import java.util.*;
import java.util.stream.Collectors;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.TestOnly;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
class PageReviewCustomFinder extends PageReviewFinder {

    @Autowired
    private PageIndexRepository pageIndexRepository;

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private CustomRepository customRepository;

    @Autowired
    private WikipediaService wikipediaService;

    @Autowired
    private FindReplacementsService findReplacementsService;

    @Setter(onMethod_ = @TestOnly)
    @Value("${replacer.indexable.namespaces}")
    private Set<Integer> indexableNamespaces;

    @Override
    PageSearchResult findPageIdsToReview(PageReviewOptions options) {
        try {
            // Initialize search
            this.incrementOffset(getCacheSize());

            WikipediaSearchResult searchResult = findWikipediaResults(options);
            final long totalWikipediaResults = searchResult.getTotal();
            long totalToReview = searchResult.getTotal();
            final List<Integer> pageIds = new LinkedList<>(searchResult.getPageIds());

            String subtype = options.getType().getSubtype();
            boolean cs = options.getCs() != null && Boolean.TRUE.equals(options.getCs());
            assert options.getType().getKind() == ReplacementKind.CUSTOM;

            // Calculate this out of the loop only if needed the first time
            List<Integer> reviewedIds = new ArrayList<>();
            if (!pageIds.isEmpty()) {
                reviewedIds.addAll(customRepository.findPageIdsReviewed(options.getLang(), subtype, cs));
            }

            while (totalToReview >= 0) {
                // Discard the pages already reviewed
                for (Integer reviewId : reviewedIds) {
                    if (pageIds.remove(reviewId)) {
                        totalToReview--;
                    }
                }

                if (pageIds.isEmpty()) {
                    this.incrementOffset(getCacheSize());
                    assert this.getOffset() != null;
                    if (this.getOffset() >= totalWikipediaResults) {
                        LOGGER.debug("All results retrieved from Wikipedia are already reviewed");
                        return PageSearchResult.ofEmpty();
                    }

                    searchResult = findWikipediaResults(options);
                    // For simplicity's sake we assume the number of total results is the same
                    pageIds.clear();
                    pageIds.addAll(searchResult.getPageIds());
                } else {
                    return PageSearchResult.of(totalToReview, pageIds);
                }
            }
        } catch (WikipediaException e) {
            LOGGER.error("Error finding page IDs in Wikipedia for options: {}", options, e);
        }

        return PageSearchResult.ofEmpty();
    }

    private WikipediaSearchResult findWikipediaResults(PageReviewOptions options) throws WikipediaException {
        String subtype = options.getType().getSubtype();
        Boolean cs = options.getCs();
        Integer offset = this.getOffset();
        assert cs != null && offset != null;
        return wikipediaService.searchByText(
            options.getLang(),
            indexableNamespaces.stream().map(WikipediaNamespace::valueOf).collect(Collectors.toUnmodifiableSet()),
            subtype,
            cs,
            offset,
            getCacheSize()
        );
    }

    @Override
    Collection<PageReplacement> findAllReplacements(WikipediaPage page, PageReviewOptions options) {
        // Add the page to the database in case it doesn't exist yet
        if (pageIndexRepository.findPageById(page.getId()).isEmpty()) {
            pageRepository.addPages(List.of(buildNewPage(page)));
        }

        // We do nothing in the database in case the list is empty
        // We want to review the page every time in case anything has changed
        return findReplacementsService.findCustomReplacements(page, options);
    }

    private PageModel buildNewPage(WikipediaPage page) {
        return PageModel
            .builder()
            .lang(page.getId().getLang().getCode())
            .pageId(page.getId().getPageId())
            .title(page.getTitle())
            .lastUpdate(page.getLastUpdate().toLocalDate())
            .replacements(Collections.emptyList())
            .build();
    }
}
