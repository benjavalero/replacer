package es.bvalero.replacer.page.review;

import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.page.PageRepository;
import es.bvalero.replacer.page.count.PageCountRepository;
import es.bvalero.replacer.page.index.PageIndexService;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import es.bvalero.replacer.wikipedia.WikipediaPageRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
class ReviewTypeFinder extends ReviewFinder {

    // Dependency injection
    private final PageRepository pageRepository;
    private final PageCountRepository pageCountRepository;

    ReviewTypeFinder(
        WikipediaPageRepository wikipediaPageRepository,
        PageIndexService pageIndexService,
        PageRepository pageRepository,
        ReviewSectionFinder reviewSectionFinder,
        PageCountRepository pageCountRepository
    ) {
        super(wikipediaPageRepository, pageIndexService, pageRepository, reviewSectionFinder);
        this.pageRepository = pageRepository;
        this.pageCountRepository = pageCountRepository;
    }

    @Override
    PageSearchResult findPageIdsToReview(ReviewOptions options) {
        int totalResults = findTotalResults(options);
        if (totalResults == 0) {
            return PageSearchResult.ofEmpty();
        }

        Collection<PageKey> pageKeys = pageRepository.findNotReviewedByType(
            options.getUser().getId().getLang(),
            options.getStandardType(),
            getCacheSize()
        );

        return PageSearchResult.of(totalResults, pageKeys);
    }

    private int findTotalResults(ReviewOptions options) {
        return pageCountRepository.countNotReviewedByType(
            options.getUser().getId().getLang(),
            options.getStandardType()
        );
    }

    @Override
    void removePageCounts(ReviewOptions options) {
        pageCountRepository.removePageCountByType(options.getUser().getId().getLang(), options.getStandardType());
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
            return List.of();
        }

        return replacements;
    }
}
