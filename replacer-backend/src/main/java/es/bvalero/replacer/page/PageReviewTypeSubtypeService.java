package es.bvalero.replacer.page;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.finder.replacement.Replacement;
import es.bvalero.replacer.finder.replacement.ReplacementFinderService;
import es.bvalero.replacer.replacement.ReplacementService;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Slf4j
@Service
class PageReviewTypeSubtypeService extends PageReviewService {

    @Autowired
    private ReplacementFinderService replacementFinderService;

    @Autowired
    private ReplacementService replacementService;

    @Override
    String buildReplacementCacheKey(PageReviewOptions options) {
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
    List<Replacement> findAllReplacements(WikipediaPage page, PageReviewOptions options) {
        List<Replacement> replacements = replacementFinderService.findList(convertToFinderPage(page));

        // We take profit and we update the database with the just calculated replacements (also when empty)
        indexReplacements(page, replacements);

        // To build the review we are only interested in the replacements of the given type and subtype
        // We can run the filter even with an empty list
        String type = options.getType();
        String subtype = options.getSubtype();
        assert type != null && subtype != null;
        return filterReplacementsByTypeAndSubtype(replacements, type, subtype);
    }

    @Override
    public void reviewPageReplacements(int pageId, PageReviewOptions options, String reviewer) {
        String type = options.getType();
        String subtype = options.getSubtype();
        assert type != null && subtype != null;
        replacementService.reviewByPageId(options.getLang(), pageId, type, subtype, reviewer);
    }

    @Loggable(prepend = true, value = Loggable.TRACE)
    private List<Replacement> filterReplacementsByTypeAndSubtype(
        List<Replacement> replacements,
        String type,
        String subtype
    ) {
        return replacements
            .stream()
            .filter(replacement -> replacement.getType().equals(type) && replacement.getSubtype().equals(subtype))
            .collect(Collectors.toList());
    }
}
