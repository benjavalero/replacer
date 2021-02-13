package es.bvalero.replacer.page;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.finder.replacement.Replacement;
import es.bvalero.replacer.finder.replacement.ReplacementFinderService;
import es.bvalero.replacer.replacement.ReplacementIndexService;
import es.bvalero.replacer.replacement.ReplacementService;
import es.bvalero.replacer.wikipedia.PageSearchResult;
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
    private ReplacementIndexService replacementIndexService;

    @Autowired
    private ReplacementService replacementService;

    @Override
    String buildReplacementCacheKey(PageReviewOptions options) {
        return String.format("%s-%s-%s", options.getLang().getCode(), options.getType(), options.getSubtype());
    }

    @Override
    PageSearchResult findPageIdsToReview(PageReviewOptions options) {
        PageRequest pagination = PageRequest.of(0, CACHE_SIZE);
        List<Integer> pageIds = replacementService.findRandomPageIdsToBeReviewedBySubtype(
            options.getLang(),
            options.getType(),
            options.getSubtype(),
            pagination
        );

        long totalResults = replacementService.countPagesToBeReviewedBySubtype(
            options.getLang(),
            options.getType(),
            options.getSubtype()
        );
        return PageSearchResult.of(totalResults, pageIds);
    }

    @Override
    List<Replacement> findAllReplacements(WikipediaPage page, PageReviewOptions options) {
        List<Replacement> replacements = replacementFinderService.findList(convertPage(page));

        // We take profit and we update the database with the just calculated replacements (also when empty)
        LOGGER.trace("Update page replacements in database");
        replacementIndexService.indexPageReplacements(toIndexable(page), replacements);

        // To build the review we are only interested in the replacements of the given type and subtype
        // We can run the filter even with an empty list
        return filterReplacementsByTypeAndSubtype(replacements, options.getType(), options.getSubtype());
    }

    @Override
    public void reviewPageReplacements(int pageId, PageReviewOptions options, String reviewer) {
        replacementService.reviewByPageId(options.getLang(), pageId, options.getType(), options.getSubtype(), reviewer);
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
