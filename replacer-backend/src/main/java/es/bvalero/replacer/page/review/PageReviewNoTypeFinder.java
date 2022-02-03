package es.bvalero.replacer.page.review;

import es.bvalero.replacer.common.domain.PageReplacement;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.repository.PageRepository;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class PageReviewNoTypeFinder extends PageReviewFinder {

    @Autowired
    private PageRepository pageRepository;

    @Override
    PageSearchResult findPageIdsToReview(PageReviewOptions options) {
        // Find a random page without filtering by type takes a lot
        // Instead find a random replacement and then the following pages
        int totalResults = pageRepository.countPagesToReview(options.getLang());
        Collection<Integer> pageIds = pageRepository.findPageIdsToReview(options.getLang(), getCacheSize());
        return PageSearchResult.of(totalResults, pageIds);
    }

    @Override
    Collection<PageReplacement> decorateReplacements(
        WikipediaPage page,
        PageReviewOptions options,
        Collection<PageReplacement> replacements
    ) {
        // No decoration needed when no type
        return replacements;
    }
}
