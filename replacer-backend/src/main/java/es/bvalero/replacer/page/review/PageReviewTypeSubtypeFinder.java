package es.bvalero.replacer.page.review;

import static es.bvalero.replacer.repository.ReplacementRepository.REVIEWER_SYSTEM;

import es.bvalero.replacer.common.domain.PageReplacement;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.page.index.PageIndexResult;
import es.bvalero.replacer.repository.PageRepository;
import es.bvalero.replacer.repository.ReplacementTypeRepository;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class PageReviewTypeSubtypeFinder extends PageReviewFinder {

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private ReplacementTypeRepository replacementTypeRepository;

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
        Collection<PageReplacement> filtered = filterReplacementsByTypeAndSubtype(
            pageIndexResult.getReplacements(),
            options
        );
        if (filtered.isEmpty()) {
            // No replacement to be reviewed for this page and type
            // We remove it from the count cache by marking it as reviewed (it should not exist in DB any more)
            replacementTypeRepository.updateReviewerByPageAndType(page.getId(), options.getType(), REVIEWER_SYSTEM);
        }

        return filtered;
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
