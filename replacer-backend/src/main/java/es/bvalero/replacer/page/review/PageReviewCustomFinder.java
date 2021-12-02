package es.bvalero.replacer.page.review;

import es.bvalero.replacer.common.domain.Replacement;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.listing.Misspelling;
import es.bvalero.replacer.finder.replacement.ReplacementMapper;
import es.bvalero.replacer.finder.replacement.ReplacementType;
import es.bvalero.replacer.finder.replacement.custom.CustomOptions;
import es.bvalero.replacer.finder.replacement.custom.CustomReplacementFinderService;
import es.bvalero.replacer.replacement.ReplacementService;
import es.bvalero.replacer.wikipedia.WikipediaSearchResult;
import es.bvalero.replacer.wikipedia.WikipediaService;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
class PageReviewCustomFinder extends PageReviewFinder {

    @Autowired
    private WikipediaService wikipediaService;

    @Autowired
    private ReplacementService replacementService;

    @Autowired
    private CustomReplacementFinderService customReplacementFinderService;

    @Override
    PageSearchResult findPageIdsToReview(PageReviewOptions options) {
        try {
            // Initialize search
            this.incrementOffset(getCacheSize());

            WikipediaSearchResult searchResult = findWikipediaResults(options);
            final long totalWikipediaResults = searchResult.getTotal();
            long totalToReview = searchResult.getTotal();
            final List<Integer> pageIds = new LinkedList<>(searchResult.getPageIds());

            String type = options.getType();
            String subtype = options.getSubtype();
            boolean cs = options.getCs() != null && Boolean.TRUE.equals(options.getCs());
            assert ReplacementType.CUSTOM.getLabel().equals(type);
            assert subtype != null;

            // Calculate this out of the loop only if needed the first time
            List<Integer> reviewedIds = new ArrayList<>();
            if (!pageIds.isEmpty()) {
                reviewedIds.addAll(replacementService.findPageIdsReviewedByReplacement(options.getLang(), subtype, cs));
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
        } catch (ReplacerException e) {
            LOGGER.error("Error finding page IDs in Wikipedia for options: {}", options, e);
        }

        return PageSearchResult.ofEmpty();
    }

    private WikipediaSearchResult findWikipediaResults(PageReviewOptions options) throws ReplacerException {
        String subtype = options.getSubtype();
        Boolean cs = options.getCs();
        Integer offset = this.getOffset();
        assert subtype != null && cs != null && offset != null;
        return wikipediaService.searchByText(options.getLang(), subtype, cs, offset, getCacheSize());
    }

    @Override
    Collection<Replacement> findAllReplacements(WikipediaPage page, PageReviewOptions options) {
        // We do nothing in the database in case the list is empty
        // We want to review the page every time in case anything has changed
        return ReplacementMapper.toDomain(
            IterableUtils.toList(customReplacementFinderService.findCustomReplacements(page, convertOptions(options)))
        );
    }

    private CustomOptions convertOptions(PageReviewOptions options) {
        String subtype = options.getSubtype();
        Boolean cs = options.getCs();
        String suggestion = options.getSuggestion();
        assert subtype != null && cs != null && suggestion != null;
        return CustomOptions.of(subtype, cs, suggestion);
    }

    ReplacementValidationResponse validateCustomReplacement(
        WikipediaLanguage lang,
        String replacement,
        boolean caseSensitive
    ) {
        Optional<Misspelling> misspelling = customReplacementFinderService.findExistingMisspelling(replacement, lang);
        if (misspelling.isEmpty()) {
            return ReplacementValidationResponse.ofEmpty();
        } else if (misspelling.get().isCaseSensitive()) {
            return caseSensitive && misspelling.get().getWord().equals(replacement)
                ? ReplacementValidationResponse.of(
                    ReplacementType.ofMisspellingType(misspelling.get()),
                    misspelling.get().getWord()
                )
                : ReplacementValidationResponse.ofEmpty();
        } else {
            return !caseSensitive && misspelling.get().getWord().equalsIgnoreCase(replacement)
                ? ReplacementValidationResponse.of(
                    ReplacementType.ofMisspellingType(misspelling.get()),
                    misspelling.get().getWord()
                )
                : ReplacementValidationResponse.ofEmpty();
        }
    }
}
