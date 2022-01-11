package es.bvalero.replacer.page.review;

import es.bvalero.replacer.common.domain.PageReplacement;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.page.index.PageIndexResult;
import es.bvalero.replacer.repository.PageRepository;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class PageReviewTypeSubtypeFinder extends PageReviewFinder {

    @Autowired
    private PageRepository pageRepository;

    @Override
    PageSearchResult findPageIdsToReview(PageReviewOptions options) {
        Collection<Integer> pageIds = pageRepository.findPageIdsToReviewByType(
            options.getLang(),
            options.getType(),
            getCacheSize()
        );

        long totalResults = pageRepository.countPagesToReviewByType(options.getLang(), options.getType());
        return PageSearchResult.of(totalResults, pageIds);
    }

    @Override
    Collection<PageReplacement> findAllReplacements(WikipediaPage page, PageReviewOptions options) {
        // We take profit, and we update the database with the just calculated replacements (also when empty).
        // If the page has not been indexed (or is not indexable) the collection of replacements will be empty
        PageIndexResult pageIndexResult = indexReplacements(page);

        // To build the review we are only interested in the replacements of the given type and subtype
        // We can run the filter even with an empty list
        return filterReplacementsByTypeAndSubtype(pageIndexResult.getReplacements(), options);
    }

    private Collection<PageReplacement> filterReplacementsByTypeAndSubtype(
        Collection<PageReplacement> replacements,
        PageReviewOptions options
    ) {
        return replacements
            .stream()
            .filter(replacement -> Objects.equals(replacement.getType(), options.getType()))
            .collect(Collectors.toUnmodifiableList());
    }
}
