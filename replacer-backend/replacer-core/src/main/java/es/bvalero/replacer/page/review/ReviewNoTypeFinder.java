package es.bvalero.replacer.page.review;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.page.PageService;
import es.bvalero.replacer.page.count.PageCountService;
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
        WikipediaLanguage lang = options.getUser().getId().getLang();
        int totalResults = pageCountService.countNotReviewedByNoType(lang);
        if (totalResults == 0) {
            return PageSearchResult.ofEmpty();
        }

        Collection<PageKey> pageKeys = pageService.findPagesToReviewByNoType(lang, getCacheSize());
        return PageSearchResult.of(totalResults, pageKeys);
    }
}
