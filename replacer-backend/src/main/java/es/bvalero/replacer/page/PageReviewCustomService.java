package es.bvalero.replacer.page;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.finder.replacement.*;
import es.bvalero.replacer.replacement.ReplacementEntity;
import es.bvalero.replacer.replacement.ReplacementService;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import es.bvalero.replacer.wikipedia.WikipediaSearchResult;
import es.bvalero.replacer.wikipedia.WikipediaService;
import java.time.LocalDate;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
class PageReviewCustomService extends PageReviewService {

    @Autowired
    private WikipediaService wikipediaService;

    @Autowired
    private ReplacementService replacementService;

    @Autowired
    private CustomReplacementFinderService customReplacementFinderService;

    @Override
    String buildReplacementCacheKey(PageReviewOptions options) {
        return String.format("%s-%s", options.getLang().getCode(), options.getSubtype());
    }

    @Override
    PageSearchResult findPageIdsToReview(PageReviewOptions options) {
        try {
            // Initialize search
            int offset = 0;

            WikipediaSearchResult searchResult = findWikipediaResults(options, offset);
            final long totalWikipediaResults = searchResult.getTotal();
            long totalToReview = searchResult.getTotal();
            final List<Integer> pageIds = new LinkedList<>(searchResult.getPageIds());

            String type = options.getType();
            String subtype = options.getSubtype();
            assert ReplacementType.CUSTOM.equals(type);
            assert subtype != null;

            // Calculate this out of the loop only if needed the first time
            List<Integer> reviewedIds = new ArrayList<>();
            if (!pageIds.isEmpty()) {
                reviewedIds.addAll(
                    replacementService.findPageIdsReviewedByTypeAndSubtype(options.getLang(), type, subtype)
                );
            }

            while (totalToReview >= 0) {
                // Discard the pages already reviewed
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

    private WikipediaSearchResult findWikipediaResults(PageReviewOptions options, int offset) throws ReplacerException {
        String subtype = options.getSubtype();
        Boolean cs = options.getCs();
        assert subtype != null && cs != null;
        return wikipediaService.getPageIdsByStringMatch(options.getLang(), subtype, cs, offset, getCacheSize());
    }

    @Override
    List<Replacement> findAllReplacements(WikipediaPage page, PageReviewOptions options) {
        // We do nothing in the database in case the list is empty
        // We want to review the page every time in case anything has changed
        return IterableUtils.toList(
            customReplacementFinderService.findCustomReplacements(convertToFinderPage(page), convertOptions(options))
        );
    }

    private CustomOptions convertOptions(PageReviewOptions options) {
        String subtype = options.getSubtype();
        Boolean cs = options.getCs();
        String suggestion = options.getSuggestion();
        assert subtype != null && cs != null && suggestion != null;
        return CustomOptions.of(subtype, cs, suggestion);
    }

    @Override
    public void reviewPageReplacements(int pageId, PageReviewOptions options, String reviewer) {
        // Custom replacements don't exist in the database to be reviewed
        String subtype = options.getSubtype();
        assert subtype != null;
        replacementService.insert(buildCustomReviewed(pageId, options.getLang(), subtype, reviewer));
    }

    private ReplacementEntity buildCustomReviewed(int pageId, WikipediaLanguage lang, String subtype, String reviewer) {
        return ReplacementEntity
            .builder()
            .lang(lang.getCode())
            .pageId(pageId)
            .type(ReplacementType.CUSTOM)
            .subtype(subtype)
            .position(0)
            .lastUpdate(LocalDate.now())
            .reviewer(reviewer)
            .build();
    }

    MisspellingType validateCustomReplacement(WikipediaLanguage lang, String replacement, boolean caseSensitive) {
        Optional<Misspelling> misspelling = customReplacementFinderService.findExistingMisspelling(replacement, lang);
        if (misspelling.isEmpty()) {
            return MisspellingType.ofEmpty();
        } else if (misspelling.get().isCaseSensitive()) {
            return caseSensitive && misspelling.get().getWord().equals(replacement)
                ? MisspellingType.of(misspelling.get().getType(), misspelling.get().getWord())
                : MisspellingType.ofEmpty();
        } else {
            return !caseSensitive && misspelling.get().getWord().equals(replacement)
                ? MisspellingType.of(misspelling.get().getType(), misspelling.get().getWord())
                : MisspellingType.ofEmpty();
        }
    }
}
