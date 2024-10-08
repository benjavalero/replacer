package es.bvalero.replacer.page.find;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.page.count.PageCountRepository;
import es.bvalero.replacer.page.index.PageIndexService;
import es.bvalero.replacer.page.save.PageSaveRepository;
import java.util.Collection;
import org.springframework.stereotype.Component;

@Component
class ReviewNoTypeFinder extends ReviewFinder {

    // Dependency injection
    private final PageRepository pageRepository;
    private final PageCountRepository pageCountRepository;

    ReviewNoTypeFinder(
        WikipediaPageRepository wikipediaPageRepository,
        PageIndexService pageIndexService,
        PageRepository pageRepository,
        PageSaveRepository pageSaveRepository,
        ReviewSectionFinder reviewSectionFinder,
        PageCountRepository pageCountRepository
    ) {
        super(wikipediaPageRepository, pageIndexService, pageRepository, pageSaveRepository, reviewSectionFinder);
        this.pageRepository = pageRepository;
        this.pageCountRepository = pageCountRepository;
    }

    @Override
    PageSearchResult findPageIdsToReview(ReviewOptions options) {
        // Find a random page without filtering by type takes a lot
        // Instead find a random replacement and then the following pages
        WikipediaLanguage lang = options.getUser().getId().getLang();
        int totalResults = pageCountRepository.countNotReviewedByType(lang, null);
        if (totalResults == 0) {
            return PageSearchResult.ofEmpty();
        }

        Collection<PageKey> pageKeys = pageRepository.findNotReviewedByType(lang, null, getCacheSize());
        return PageSearchResult.of(totalResults, pageKeys);
    }

    @Override
    Collection<Replacement> decorateReplacements(
        WikipediaPage page,
        ReviewOptions options,
        Collection<Replacement> replacements
    ) {
        // No decoration by default
        return replacements;
    }
}
