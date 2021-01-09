package es.bvalero.replacer.page;

import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.finder.FinderUtils;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFindService;
import es.bvalero.replacer.finder.misspelling.MisspellingComposedFinder;
import es.bvalero.replacer.finder.misspelling.MisspellingSimpleFinder;
import es.bvalero.replacer.replacement.ReplacementDao;
import es.bvalero.replacer.replacement.ReplacementEntity;
import es.bvalero.replacer.wikipedia.PageSearchResult;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import es.bvalero.replacer.wikipedia.WikipediaService;
import java.util.*;
import javax.annotation.Resource;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
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
    private ReplacementDao replacementDao;

    @Autowired
    private ReplacementFindService replacementFindService;

    @Autowired
    private MisspellingSimpleFinder misspellingSimpleFinder;

    @Autowired
    private MisspellingComposedFinder misspellingComposedFinder;

    @Getter
    @Setter
    @Resource
    private List<String> ignorableTemplates;

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

            List<Integer> reviewedIds = replacementDao.findPageIdsReviewedByCustomTypeAndSubtype(
                options.getLang(),
                options.getSubtype()
            );

            PageSearchResult pageIds = wikipediaService.getPageIdsByStringMatch(
                options.getSubtype(),
                caseSensitive,
                offset,
                CACHE_SIZE,
                options.getLang()
            );
            while (!pageIds.isEmpty()) {
                // Discard the pages already reviewed
                pageIds.removePageIds(reviewedIds);

                if (pageIds.isEmpty()) {
                    offset += CACHE_SIZE;
                    cachedOffsets.put(cacheKey, offset);
                    pageIds =
                        wikipediaService.getPageIdsByStringMatch(
                            options.getSubtype(),
                            caseSensitive,
                            offset,
                            CACHE_SIZE,
                            options.getLang()
                        );
                } else {
                    return pageIds;
                }
            }
        } catch (ReplacerException e) {
            LOGGER.error("Error finding page IDs in Wikipedia for options: {}", options, e);
        }

        return PageSearchResult.ofEmpty();
    }

    @Override
    void setPageAsReviewed(int pageId, PageReviewOptions options) {
        // Do nothing
    }

    @Override
    List<Replacement> findAllReplacements(WikipediaPage page, PageReviewOptions options) {
        // We do nothing in the database in case the list is empty
        // We want to review the page every time in case anything has changed
        return replacementFindService.findCustomReplacements(page, options.getSubtype(), options.getSuggestion());
    }

    void reviewPageReplacements(int pageId, WikipediaLanguage lang, String subtype, String reviewer) {
        // Custom replacements don't exist in the database to be reviewed
        replacementDao.insert(ReplacementEntity.createCustomReviewed(pageId, lang, subtype, reviewer));
    }

    Optional<String> validateCustomReplacement(String replacement, WikipediaLanguage lang) {
        if (misspellingSimpleFinder.findMisspellingByWord(replacement, lang).isPresent()) {
            return Optional.of(misspellingSimpleFinder.getType());
        } else if (misspellingComposedFinder.findMisspellingByWord(replacement, lang).isPresent()) {
            return Optional.of(misspellingComposedFinder.getType());
        } else {
            return Optional.empty();
        }
    }
}
