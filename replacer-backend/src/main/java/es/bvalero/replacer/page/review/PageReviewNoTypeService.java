package es.bvalero.replacer.page.review;

import es.bvalero.replacer.common.domain.Replacement;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.page.index.PageIndexResult;
import es.bvalero.replacer.replacement.ReplacementService;
import java.util.Collection;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PageReviewNoTypeService extends PageReviewService {

    // TODO: Public while refactoring

    @Autowired
    private ReplacementService replacementService;

    @Override
    String buildCacheKey(PageReviewOptions options) {
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
    Collection<Replacement> findAllReplacements(WikipediaPage page, PageReviewOptions options) {
        // We take profit, and we update the database with the just calculated replacements (also when empty).
        // If the page has not been indexed (or is not indexable) the collection of replacements is empty
        PageIndexResult pageIndexResult = indexReplacements(page);

        return pageIndexResult.getReplacements();
    }

    @Override
    public void reviewPageReplacements(int pageId, PageReviewOptions options, String reviewer) {
        replacementService.reviewByPageId(options.getLang(), pageId, null, null, reviewer);
    }
}
