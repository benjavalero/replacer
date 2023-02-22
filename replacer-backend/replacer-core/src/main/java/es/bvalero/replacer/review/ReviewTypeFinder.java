package es.bvalero.replacer.review;

import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.page.PageCountService;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.page.PageService;
import es.bvalero.replacer.replacement.ReplacementService;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class ReviewTypeFinder extends ReviewFinder {

    @Autowired
    private PageService pageService;

    @Autowired
    private PageCountService pageCountService;

    @Autowired
    private ReplacementService replacementService;

    @Override
    PageSearchResult findPageIdsToReview(ReviewOptions options) {
        int totalResults = findTotalResults(options);
        if (totalResults == 0) {
            return PageSearchResult.ofEmpty();
        }

        Collection<PageKey> pageKeys = pageService.findPagesToReviewByType(
            options.getUserId().getLang(),
            options.getType(),
            getCacheSize()
        );

        return PageSearchResult.of(totalResults, pageKeys);
    }

    private int findTotalResults(ReviewOptions options) {
        return pageCountService.countPagesToReviewByType(options.getUserId().getLang(), options.getType());
    }

    @Override
    Collection<Replacement> decorateReplacements(
        WikipediaPage page,
        ReviewOptions options,
        Collection<Replacement> replacements
    ) {
        // Though the whole list of replacements will be returned no matter the type
        // we run a filter to check there is at least one replacement of the requested type
        Collection<Replacement> filtered = filterReplacementsByType(replacements, options);
        if (filtered.isEmpty()) {
            // No replacement to be reviewed for this page and type
            // We remove it from the count cache by marking it as reviewed (it should not exist in DB anymore)
            replacementService.reviewReplacementsByPageAndType(page.getPageKey(), options.getType());
            return Collections.emptyList();
        }

        return replacements;
    }
}
