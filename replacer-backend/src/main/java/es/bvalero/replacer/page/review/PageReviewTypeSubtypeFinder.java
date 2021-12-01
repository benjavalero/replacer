package es.bvalero.replacer.page.review;

import es.bvalero.replacer.common.domain.Replacement;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.page.index.PageIndexResult;
import es.bvalero.replacer.replacement.ReplacementService;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class PageReviewTypeSubtypeFinder extends PageReviewFinder {

    // TODO: Call directly to the repository
    @Autowired
    private ReplacementService replacementService;

    @Override
    String buildCacheKey(PageReviewOptions options) {
        return String.format("%s-%s-%s", options.getLang().getCode(), options.getType(), options.getSubtype());
    }

    @Override
    PageSearchResult findPageIdsToReview(PageReviewOptions options) {
        PageRequest pagination = PageRequest.of(0, getCacheSize());
        String type = options.getType();
        String subtype = options.getSubtype();
        assert type != null && subtype != null;
        List<Integer> pageIds = replacementService.findRandomPageIdsToBeReviewedBySubtype(
            options.getLang(),
            type,
            subtype,
            pagination
        );

        long totalResults = replacementService.countPagesToBeReviewedBySubtype(options.getLang(), type, subtype);
        return PageSearchResult.of(totalResults, pageIds);
    }

    @Override
    Collection<Replacement> findAllReplacements(WikipediaPage page, PageReviewOptions options) {
        // We take profit, and we update the database with the just calculated replacements (also when empty).
        // If the page has not been indexed (or is not indexable) the collection of replacements will be empty
        PageIndexResult pageIndexResult = indexReplacements(page);

        // To build the review we are only interested in the replacements of the given type and subtype
        // We can run the filter even with an empty list
        String type = options.getType();
        String subtype = options.getSubtype();
        assert type != null && subtype != null;
        return filterReplacementsByTypeAndSubtype(pageIndexResult.getReplacements(), type, subtype);
    }

    @Override
    public void reviewPageReplacements(int pageId, PageReviewOptions options, String reviewer) {
        String type = options.getType();
        String subtype = options.getSubtype();
        assert type != null && subtype != null;
        replacementService.reviewByPageId(options.getLang(), pageId, type, subtype, reviewer);
    }

    private Collection<Replacement> filterReplacementsByTypeAndSubtype(
        Collection<Replacement> replacements,
        String type,
        String subtype
    ) {
        return replacements
            .stream()
            .filter(
                replacement -> replacement.getType().getLabel().equals(type) && replacement.getSubtype().equals(subtype)
            )
            .collect(Collectors.toList());
    }
}
