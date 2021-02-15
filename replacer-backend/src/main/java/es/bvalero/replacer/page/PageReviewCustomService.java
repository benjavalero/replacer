package es.bvalero.replacer.page;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.finder.replacement.CustomReplacementFinderService;
import es.bvalero.replacer.finder.replacement.Replacement;
import es.bvalero.replacer.finder.replacement.ReplacementType;
import es.bvalero.replacer.finder.util.FinderUtils;
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

    // Cache the offsets to find custom replacements with Wikipedia Search API
    private final Map<String, Integer> cachedOffsets = new HashMap<>();

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
            boolean caseSensitive =
                FinderUtils.containsUppercase(options.getSubtype()) ||
                FinderUtils.containsUppercase(options.getSuggestion());

            // Find cached offset
            int offset = 0;
            String cacheKey = buildReplacementCacheKey(options);
            if (cachedOffsets.containsKey(cacheKey)) {
                offset = cachedOffsets.get(cacheKey);
            } else {
                cachedOffsets.put(cacheKey, offset);
            }

            List<Integer> reviewedIds = replacementService.findPageIdsReviewedByTypeAndSubtype(
                options.getLang(),
                ReplacementType.CUSTOM,
                options.getSubtype()
            );

            PageSearchResult pageIds = convert(
                wikipediaService.getPageIdsByStringMatch(
                    options.getLang(),
                    options.getSubtype(),
                    caseSensitive,
                    offset,
                    CACHE_SIZE
                )
            );
            offset += CACHE_SIZE;
            cachedOffsets.put(cacheKey, offset);

            while (!pageIds.isEmpty()) {
                // Discard the pages already reviewed
                pageIds.removePageIds(reviewedIds);

                if (pageIds.isEmpty()) {
                    pageIds =
                        convert(
                            wikipediaService.getPageIdsByStringMatch(
                                options.getLang(),
                                options.getSubtype(),
                                caseSensitive,
                                offset,
                                CACHE_SIZE
                            )
                        );
                    offset += CACHE_SIZE;
                    cachedOffsets.put(cacheKey, offset);
                } else {
                    return pageIds;
                }
            }
        } catch (ReplacerException e) {
            LOGGER.error("Error finding page IDs in Wikipedia for options: {}", options, e);
        }

        return PageSearchResult.ofEmpty();
    }

    private PageSearchResult convert(WikipediaSearchResult wikipediaSearchResult) {
        return PageSearchResult.of(wikipediaSearchResult.getTotal(), wikipediaSearchResult.getPageIds());
    }

    @Override
    List<Replacement> findAllReplacements(WikipediaPage page, PageReviewOptions options) {
        // We do nothing in the database in case the list is empty
        // We want to review the page every time in case anything has changed
        return IterableUtils.toList(
            customReplacementFinderService.findCustomReplacements(convertPage(page), options.toCustomOptions())
        );
    }

    @Override
    public void reviewPageReplacements(int pageId, PageReviewOptions options, String reviewer) {
        // Custom replacements don't exist in the database to be reviewed
        replacementService.insert(buildCustomReviewed(pageId, options.getLang(), options.getSubtype(), reviewer));
    }

    private ReplacementEntity buildCustomReviewed(int pageId, WikipediaLanguage lang, String subtype, String reviewer) {
        return ReplacementEntity
            .builder()
            .pageId(pageId)
            .lang(lang.getCode())
            .type(ReplacementType.CUSTOM)
            .subtype(subtype)
            .position(0)
            .lastUpdate(LocalDate.now())
            .reviewer(reviewer)
            .build();
    }

    Optional<String> validateCustomReplacement(String replacement, WikipediaLanguage lang) {
        return customReplacementFinderService.findExistingMisspelling(replacement, lang);
    }
}
