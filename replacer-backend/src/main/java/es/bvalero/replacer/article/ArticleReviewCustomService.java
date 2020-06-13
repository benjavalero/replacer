package es.bvalero.replacer.article;

import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFindService;
import es.bvalero.replacer.replacement.ReplacementIndexService;
import es.bvalero.replacer.replacement.ReplacementRepository;
import es.bvalero.replacer.wikipedia.PageSearchResult;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import es.bvalero.replacer.wikipedia.WikipediaService;
import java.util.List;
import javax.annotation.Resource;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
class ArticleReviewCustomService extends ArticleReviewService {
    @Autowired
    private WikipediaService wikipediaService;

    @Autowired
    private ReplacementRepository replacementRepository;

    @Autowired
    private ReplacementFindService replacementFindService;

    @Autowired
    private ReplacementIndexService replacementIndexService;

    @Getter
    @Setter
    @Resource
    private List<String> ignorableTemplates;

    @Override
    String buildReplacementCacheKey(ArticleReviewOptions options) {
        return String.format("%s-%s-%s", options.getLang().getCode(), options.getSubtype(), options.getSuggestion());
    }

    @Override
    PageSearchResult findPageIdsToReview(ArticleReviewOptions options) {
        try {
            int offset = 0;
            List<Integer> reviewedIds = replacementRepository.findByLangAndTypeAndSubtypeAndReviewerNotNull(
                options.getLang().getCode(),
                options.getType(),
                options.getSubtype()
            );

            // We need a List in order to use "removeIf"
            PageSearchResult pageIds = wikipediaService.getPageIdsByStringMatch(
                options.getSubtype(),
                offset,
                CACHE_SIZE,
                options.getLang()
            );
            while (!pageIds.isEmpty()) {
                // Discard the pages already reviewed
                // TODO: We should only mark as reviewed a custom replacement if it has been reviewed without changes
                pageIds.removePageIds(reviewedIds);

                if (pageIds.isEmpty()) {
                    offset += CACHE_SIZE;
                    pageIds =
                        wikipediaService.getPageIdsByStringMatch(
                            options.getSubtype(),
                            offset,
                            CACHE_SIZE,
                            options.getLang()
                        );
                } else {
                    return pageIds;
                }
            }
        } catch (ReplacerException e) {
            LOGGER.error("Error searching page IDs from Wikipedia", e);
        }

        return PageSearchResult.ofEmpty();
    }

    @Override
    void setArticleAsReviewed(int articleId, ArticleReviewOptions options) {
        // We add the custom replacement to the database as reviewed to skip it after the next search in the API
        replacementIndexService.addCustomReviewedReplacement(articleId, options.getLang(), options.getSubtype());
    }

    @Override
    List<Replacement> findAllReplacements(WikipediaPage article, ArticleReviewOptions options) {
        List<Replacement> replacements = replacementFindService.findCustomReplacements(
            article.getContent(),
            options.getSubtype(),
            options.getSuggestion(),
            article.getLang()
        );

        if (replacements.isEmpty()) {
            // We add the custom replacement to the database as reviewed to skip it after the next search in the API
            replacementIndexService.addCustomReviewedReplacement(
                article.getId(),
                article.getLang(),
                options.getSubtype()
            );
        }

        return replacements;
    }
}
