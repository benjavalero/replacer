package es.bvalero.replacer.review.find;

import static es.bvalero.replacer.repository.ReplacementRepository.REVIEWER_SYSTEM;

import es.bvalero.replacer.common.domain.Replacement;
import es.bvalero.replacer.common.domain.ReviewOptions;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.repository.PageRepository;
import es.bvalero.replacer.repository.ReplacementTypeRepository;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class PageReviewTypeFinder extends PageReviewFinder {

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private ReplacementTypeRepository replacementTypeRepository;

    @Override
    PageSearchResult findPageIdsToReview(ReviewOptions options) {
        Collection<Integer> pageIds = pageRepository.findPageIdsToReviewByType(
            options.getLang(),
            options.getType(),
            getCacheSize()
        );

        int totalResults = pageRepository.countPagesToReviewByType(options.getLang(), options.getType());
        return PageSearchResult.of(totalResults, pageIds);
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
            // We remove it from the count cache by marking it as reviewed (it should not exist in DB any more)
            replacementTypeRepository.updateReviewerByPageAndType(page.getId(), options.getType(), REVIEWER_SYSTEM);
            return Collections.emptyList();
        }

        return replacements;
    }

    private Collection<Replacement> filterReplacementsByType(
        Collection<Replacement> replacements,
        ReviewOptions options
    ) {
        return replacements
            .stream()
            .filter(replacement -> Objects.equals(replacement.getType(), options.getType()))
            .collect(Collectors.toUnmodifiableList());
    }
}
