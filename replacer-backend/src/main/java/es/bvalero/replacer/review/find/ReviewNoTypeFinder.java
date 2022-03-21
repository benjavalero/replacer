package es.bvalero.replacer.review.find;

import es.bvalero.replacer.common.domain.Replacement;
import es.bvalero.replacer.common.domain.ReviewOptions;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.repository.PageRepository;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class ReviewNoTypeFinder extends ReviewFinder {

    @Autowired
    private PageRepository pageRepository;

    @Override
    PageSearchResult findPageIdsToReview(ReviewOptions options) {
        // Find a random page without filtering by type takes a lot
        // Instead find a random replacement and then the following pages
        int totalResults = pageRepository.countPagesToReview(options.getLang());
        Collection<Integer> pageIds = pageRepository.findPageIdsToReview(options.getLang(), getCacheSize());
        return PageSearchResult.of(totalResults, pageIds);
    }

    @Override
    Collection<Replacement> decorateReplacements(
        WikipediaPage page,
        ReviewOptions options,
        Collection<Replacement> replacements
    ) {
        // No decoration needed when no type
        return replacements;
    }
}
