package es.bvalero.replacer.page.review;

import es.bvalero.replacer.common.domain.Replacement;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.repository.PageReviewRepository;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class PageReviewNoTypeFinder extends PageReviewFinder {

    @Autowired
    private PageReviewRepository pageReviewRepository;

    @Override
    PageSearchResult findPageIdsToReview(PageReviewOptions options) {
        // Find a random page without filtering by type takes a lot
        // Instead find a random replacement and then the following pages
        long totalResults = pageReviewRepository.countToReview(options.getLang());
        Collection<Integer> pageIds = pageReviewRepository.findToReview(options.getLang(), getCacheSize());
        return PageSearchResult.of(totalResults, pageIds);
    }

    @Override
    Collection<Replacement> findAllReplacements(WikipediaPage page, PageReviewOptions options) {
        // We take profit, and we update the database with the just calculated replacements (also when empty).
        // If the page has not been indexed (or is not indexable) the collection of replacements is empty
        return indexReplacements(page).getReplacements();
    }
}
