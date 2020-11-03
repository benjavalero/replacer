package es.bvalero.replacer.page;

import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFindService;
import es.bvalero.replacer.replacement.ReplacementDao;
import es.bvalero.replacer.replacement.ReplacementEntity;
import es.bvalero.replacer.wikipedia.PageSearchResult;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import es.bvalero.replacer.wikipedia.WikipediaService;
import java.util.List;
import javax.annotation.Resource;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
class PageReviewCustomService extends PageReviewService {
    @Autowired
    private WikipediaService wikipediaService;

    @Autowired
    private ReplacementDao replacementDao;

    @Autowired
    private ReplacementFindService replacementFindService;

    @Getter
    @Setter
    @Resource
    private List<String> ignorableTemplates;

    @Override
    String buildReplacementCacheKey(PageReviewOptions options) {
        return String.format("%s-%s-%s", options.getLang().getCode(), options.getSubtype(), options.getSuggestion());
    }

    @Override
    PageSearchResult findPageIdsToReview(PageReviewOptions options) {
        try {
            int offset = 0;
            List<Integer> reviewedIds = replacementDao.findPageIdsReviewedByCustomTypeAndSubtype(
                options.getLang(),
                options.getSubtype()
            );

            // We need a List in order to use "removeIf"
            PageSearchResult pageIds = wikipediaService.getPageIdsByStringMatch(
                options.getSubtype(),
                offset,
                CACHE_SIZE,
                options.getLang()
            );
            while (!pageIds.isEmpty()) {
                // Discard the pages already reviewed
                // TODO: We should only mark as reviewed a custom replacement if it has been reviewed without changes
                pageIds.removePageIds(reviewedIds);

                if (pageIds.isEmpty()) {
                    offset += CACHE_SIZE;
                    pageIds =
                        wikipediaService.getPageIdsByStringMatch(
                            options.getSubtype(),
                            offset,
                            CACHE_SIZE,
                            options.getLang()
                        );
                } else {
                    return pageIds;
                }
            }
        } catch (ReplacerException e) {
            LOGGER.error("Error searching page IDs from Wikipedia", e);
        }

        return PageSearchResult.ofEmpty();
    }

    @Override
    void setPageAsReviewed(int pageId, PageReviewOptions options) {
        // We add the custom replacement to the database as reviewed to skip it after the next search in the API
        addCustomSystemReviewedReplacement(pageId, options.getLang(), options.getSubtype());
    }

    @Override
    List<Replacement> findAllReplacements(WikipediaPage page, PageReviewOptions options) {
        List<Replacement> replacements = replacementFindService.findCustomReplacements(
            page.getContent(),
            options.getSubtype(),
            options.getSuggestion(),
            page.getLang()
        );

        if (replacements.isEmpty()) {
            // We add the custom replacement to the database as reviewed to skip it after the next search in the API
            addCustomSystemReviewedReplacement(page.getId(), page.getLang(), options.getSubtype());
        }

        return replacements;
    }

    private void addCustomSystemReviewedReplacement(int pageId, WikipediaLanguage lang, String replacement) {
        replacementDao.insert(ReplacementEntity.createCustomSystemReviewed(pageId, lang, replacement));
    }

    void reviewPageReplacements(int pageId, WikipediaLanguage lang, String subtype, String reviewer) {
        // Custom replacements don't exist in the database to be reviewed
        replacementDao.insert(ReplacementEntity.createCustomReviewed(pageId, lang, subtype, reviewer));
    }
}
