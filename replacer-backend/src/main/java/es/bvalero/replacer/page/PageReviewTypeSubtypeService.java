package es.bvalero.replacer.page;

import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFindService;
import es.bvalero.replacer.replacement.ReplacementCountService;
import es.bvalero.replacer.replacement.ReplacementDao;
import es.bvalero.replacer.replacement.ReplacementIndexService;
import es.bvalero.replacer.wikipedia.PageSearchResult;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
class PageReviewTypeSubtypeService extends PageReviewService {
    @Autowired
    private ReplacementFindService replacementFindService;

    @Autowired
    private ReplacementIndexService replacementIndexService;

    @Autowired
    private ReplacementDao replacementDao;

    @Autowired
    private ReplacementCountService replacementCountService;

    @Getter
    @Setter
    @Resource
    private List<String> ignorableTemplates;

    @Override
    String buildReplacementCacheKey(PageReviewOptions options) {
        return String.format("%s-%s-%s", options.getLang().getCode(), options.getType(), options.getSubtype());
    }

    @Override
    PageSearchResult findPageIdsToReview(PageReviewOptions options) {
        PageRequest pagination = PageRequest.of(0, CACHE_SIZE);
        List<Integer> pageIds = replacementDao.findRandomPageIdsToBeReviewedBySubtype(
            options.getLang(),
            options.getType(),
            options.getSubtype(),
            pagination
        );

        if (pageIds.isEmpty()) {
            // If finally there are no results empty the cached count for the replacement
            // No need to check if there exists something cached
            replacementCountService.removeCachedReplacementCount(
                options.getLang(),
                options.getType(),
                options.getSubtype()
            );
        }

        long totalResults = replacementDao.countPagesToBeReviewedBySubtype(
            options.getLang(),
            options.getType(),
            options.getSubtype()
        );
        return new PageSearchResult(totalResults, pageIds);
    }

    @Override
    List<Replacement> findAllReplacements(WikipediaPage page, PageReviewOptions options) {
        List<Replacement> replacements = replacementFindService.findReplacements(page.getContent(), page.getLang());

        // We take profit and we update the database with the just calculated replacements (also when empty)
        LOGGER.debug("Update page replacements in database");
        replacementIndexService.indexPageReplacements(
            page,
            replacements.stream().map(page::convertReplacementToIndexed).collect(Collectors.toList())
        );

        // To build the review we are only interested in the replacements of the given type and subtype
        // We can run the filter even with an empty list
        replacements = filterReplacementsByTypeAndSubtype(replacements, options.getType(), options.getSubtype());
        LOGGER.debug("Final replacements found in text after filtering: {}", replacements.size());

        return replacements;
    }

    void reviewPageReplacements(int pageId, WikipediaLanguage lang, String type, String subtype, String reviewer) {
        replacementDao.reviewByPageId(lang, pageId, type, subtype, reviewer);

        // Decrease the cached count (one page)
        replacementCountService.decreaseCachedReplacementsCount(lang, type, subtype, 1);
    }

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
