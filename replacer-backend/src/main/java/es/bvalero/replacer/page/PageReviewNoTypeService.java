package es.bvalero.replacer.page;

import es.bvalero.replacer.finder.replacement.Replacement;
import es.bvalero.replacer.finder.replacement.ReplacementFinderService;
import es.bvalero.replacer.replacement.ReplacementIndexService;
import es.bvalero.replacer.replacement.ReplacementService;
import es.bvalero.replacer.wikipedia.WikipediaPage;
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
    private ReplacementIndexService replacementIndexService;

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
        PageRequest pagination = PageRequest.of(0, CACHE_SIZE);
        long randomStart = replacementService.findRandomIdToBeReviewed(options.getLang(), CACHE_SIZE);
        long totalResults = replacementService.countReplacementsNotReviewed(options.getLang());
        List<Integer> pageIds = replacementService.findPageIdsToBeReviewed(options.getLang(), randomStart, pagination);
        return PageSearchResult.of(totalResults, pageIds);
    }

    @Override
    List<Replacement> findAllReplacements(WikipediaPage page, PageReviewOptions options) {
        List<Replacement> replacements = replacementFinderService.findList(convertPage(page));

        // We take profit and we update the database with the just calculated replacements (also when empty)
        LOGGER.trace("Update page replacements in database");
        replacementIndexService.indexPageReplacements(toIndexable(page), replacements);

        return replacements;
    }

    @Override
    public void reviewPageReplacements(int pageId, PageReviewOptions options, String reviewer) {
        replacementService.reviewByPageId(options.getLang(), pageId, null, null, reviewer);
    }
}
