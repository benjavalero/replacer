package es.bvalero.replacer.review;

import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.page.PageCountService;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.page.PageService;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class ReviewNoTypeFinder extends ReviewFinder {

    @Autowired
    private PageService pageService;

    @Autowired
    private PageCountService pageCountService;

    @Override
    PageSearchResult findPageIdsToReview(ReviewOptions options) {
        // Find a random page without filtering by type takes a lot
        // Instead find a random replacement and then the following pages
        int totalResults = pageCountService.countPagesToReviewByNoType(options.getUserId().getLang());
        Collection<PageKey> pageKeys = pageService.findPagesToReviewByNoType(
            options.getUserId().getLang(),
            getCacheSize()
        );
        return PageSearchResult.of(totalResults, pageKeys);
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
