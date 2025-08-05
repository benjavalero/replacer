package es.bvalero.replacer.page.find;

import es.bvalero.replacer.common.domain.PageKey;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.StandardType;
import es.bvalero.replacer.page.count.PageCountRepository;
import es.bvalero.replacer.page.index.PageIndexService;
import es.bvalero.replacer.page.save.PageSaveRepository;
import es.bvalero.replacer.replacement.save.ReplacementSaveRepository;
import es.bvalero.replacer.replacement.type.ReplacementTypeSaveApi;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import java.util.Collection;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
class ReviewTypeFinder extends ReviewFinder {

    // Dependency injection
    private final PageRepository pageRepository;
    private final PageSaveRepository pageSaveRepository;
    private final PageCountRepository pageCountRepository;
    private final ReplacementSaveRepository replacementSaveRepository;
    private final ReplacementTypeSaveApi replacementTypeSaveApi;

    ReviewTypeFinder(
        WikipediaPageRepository wikipediaPageRepository,
        PageIndexService pageIndexService,
        PageRepository pageRepository,
        PageSaveRepository pageSaveRepository,
        ReviewSectionFinder reviewSectionFinder,
        PageCountRepository pageCountRepository,
        ReplacementSaveRepository replacementSaveRepository,
        ReplacementTypeSaveApi replacementTypeSaveApi
    ) {
        super(wikipediaPageRepository, pageIndexService, pageRepository, pageSaveRepository, reviewSectionFinder);
        this.pageRepository = pageRepository;
        this.pageSaveRepository = pageSaveRepository;
        this.pageCountRepository = pageCountRepository;
        this.replacementSaveRepository = replacementSaveRepository;
        this.replacementTypeSaveApi = replacementTypeSaveApi;
    }

    @Override
    PageSearchResult findPageIdsToReview(ReviewOptions options) {
        int totalResults = findTotalResults(options);
        if (totalResults == 0) {
            LOGGER.debug("No results on DB. Re-index with some Wikipedia live results and retry.");
            forceIndex(options);
            totalResults = findTotalResults(options);
            if (totalResults == 0) {
                return PageSearchResult.ofEmpty();
            }
        }

        return PageSearchResult.of(totalResults, findResults(options));
    }

    private int findTotalResults(ReviewOptions options) {
        return pageCountRepository.countNotReviewedByType(
            options.getUser().getId().getLang(),
            options.getStandardType()
        );
    }

    private Collection<PageKey> findResults(ReviewOptions options) {
        return pageRepository.findNotReviewedByType(
            options.getUser().getId().getLang(),
            options.getStandardType(),
            getCacheSize()
        );
    }

    private void forceIndex(ReviewOptions options) {
        replacementTypeSaveApi.index(options.getUser().getId().getLang(), options.getStandardType());
    }

    @Override
    void removePageCounts(ReviewOptions options) {
        replacementSaveRepository.removeByType(options.getUser().getId().getLang(), options.getStandardType());
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
