package es.bvalero.replacer.article;

import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFindService;
import es.bvalero.replacer.replacement.ReplacementIndexService;
import es.bvalero.replacer.replacement.ReplacementRepository;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import es.bvalero.replacer.wikipedia.WikipediaService;
import java.util.Collections;
import java.util.List;
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

    @Override
    String buildReplacementCacheKey(ArticleReviewOptions options) {
        return String.format("%s-%s-%s", options.getLang().getCode(), options.getSubtype(), options.getSuggestion());
    }

    @Override
    List<Integer> findArticleIdsToReview(ArticleReviewOptions options) {
        try {
            int offset = 0;
            // We need a List in order to use "removeIf"
            List<Integer> articleIds = wikipediaService.getPageIdsByStringMatch(
                options.getSubtype(),
                offset,
                CACHE_SIZE,
                options.getLang()
            );
            while (!articleIds.isEmpty()) {
                // Discard the pages already reviewed
                List<Integer> reviewedIds = replacementRepository.findByArticleIdInAndLangAndTypeAndSubtypeAndReviewerNotNull(
                    articleIds,
                    options.getLang().getCode(),
                    options.getType(),
                    options.getSubtype()
                );
                articleIds.removeAll(reviewedIds);

                if (articleIds.isEmpty()) {
                    offset += CACHE_SIZE;
                    articleIds =
                        wikipediaService.getPageIdsByStringMatch(
                            options.getSubtype(),
                            offset,
                            CACHE_SIZE,
                            options.getLang()
                        );
                } else {
                    return articleIds;
                }
            }
        } catch (ReplacerException e) {
            LOGGER.error("Error searching page IDs from Wikipedia", e);
        }

        return Collections.emptyList();
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
