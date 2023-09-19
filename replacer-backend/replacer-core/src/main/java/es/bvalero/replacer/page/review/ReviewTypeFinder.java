package es.bvalero.replacer.page.review;

import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.page.PageService;
import es.bvalero.replacer.page.count.PageCountService;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import java.util.Collection;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class ReviewTypeFinder extends ReviewFinder {

    @Autowired
    private PageService pageService;

    @Autowired
    private PageCountService pageCountService;

    @Override
    PageSearchResult findPageIdsToReview(ReviewOptions options) {
        int totalResults = findTotalResults(options);
        if (totalResults == 0) {
            return PageSearchResult.ofEmpty();
        }

        Collection<PageKey> pageKeys = pageService.findPagesToReviewByType(
            options.getUser().getId().getLang(),
            options.getStandardType(),
            getCacheSize()
        );

        return PageSearchResult.of(totalResults, pageKeys);
    }

    private int findTotalResults(ReviewOptions options) {
        return pageCountService.countNotReviewedByType(options.getUser().getId().getLang(), options.getStandardType());
    }

    @Override
    Collection<Replacement> decorateReplacements(
        WikipediaPage page,
        ReviewOptions options,
        Collection<Replacement> replacements
    ) {
        // Though the whole list of replacements will be returned no matter the type
        // we run a filter to check there is at least one replacement of the requested type
        StandardType type = options.getStandardType();
        Collection<Replacement> filtered = filterReplacementsByType(replacements, type);
        if (filtered.isEmpty()) {
            // No replacement to be reviewed for this page and type
            return Collections.emptyList();
        }

        return replacements;
    }
}
