package es.bvalero.replacer.page;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.replacement.Replacement;
import es.bvalero.replacer.finder.replacement.ReplacementFinderService;
import es.bvalero.replacer.page.index.PageNotProcessableException;
import es.bvalero.replacer.replacement.ReplacementService;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Slf4j
@Service
class PageReviewNoTypeService extends PageReviewService {

    @Autowired
    private ReplacementFinderService replacementFinderService;

    @Autowired
    private ReplacementService replacementService;

    @Override
    String buildReplacementCacheKey(PageReviewOptions options) {
        return options.getLang().getCode();
    }

    @Override
    PageSearchResult findPageIdsToReview(PageReviewOptions options) {
        // Find a random page without filtering by type takes a lot
        // Instead find a random replacement and then the following pages
        PageRequest pagination = PageRequest.of(0, getCacheSize());
        long randomStart = replacementService.findRandomIdToBeReviewed(options.getLang(), getCacheSize());
        long totalResults = replacementService.countReplacementsNotReviewed(options.getLang());
        List<Integer> pageIds = replacementService.findPageIdsToBeReviewed(options.getLang(), randomStart, pagination);
        return PageSearchResult.of(totalResults, pageIds);
    }

    @Override
    List<Replacement> findAllReplacements(WikipediaPage page, PageReviewOptions options) {
        List<Replacement> replacements = replacementFinderService.find(convertToFinderPage(page));

        // We take profit, and we update the database with the just calculated replacements (also when empty).
        try {
            indexReplacements(page, replacements);
        } catch (PageNotProcessableException e) {
            // Page not processable
            return Collections.emptyList();
        }

        return replacements;
    }

    @Override
    public void reviewPageReplacements(int pageId, PageReviewOptions options, String reviewer) {
        replacementService.reviewByPageId(options.getLang(), pageId, null, null, reviewer);
    }
}
