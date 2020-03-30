package es.bvalero.replacer.article;

import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFindService;
import es.bvalero.replacer.replacement.ReplacementIndexService;
import es.bvalero.replacer.replacement.ReplacementRepository;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import es.bvalero.replacer.wikipedia.WikipediaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
class ArticleReviewCustomService extends ArticleReviewCachedService {

    @Autowired
    private WikipediaService wikipediaService;

    @Autowired
    private ReplacementRepository replacementRepository;

    @Autowired
    private ReplacementFindService replacementFindService;

    @Autowired
    private ReplacementIndexService replacementIndexService;

    @Override
    List<Integer> findArticleIdsToReview(ArticleReviewOptions options) {
        try {
            List<Integer> articleIds = new ArrayList<>(wikipediaService.getPageIdsByStringMatch(options.getSubtype()));

            // Check that the replacement has not already been reviewed
            articleIds.removeIf(id -> replacementRepository.countByArticleIdAndTypeAndSubtypeAndReviewerNotNull(
                    id, options.getType(), options.getSubtype()) > 0);

            return articleIds;
        } catch (WikipediaException e) {
            LOGGER.error("Error searching page IDs from Wikipedia", e);
            return Collections.emptyList();
        }
    }

    @Override
    Optional<WikipediaPage> getArticleFromWikipedia(int articleId, ArticleReviewOptions options) {
        Optional<WikipediaPage> article = super.getArticleFromWikipedia(articleId, options);

        if (article.isEmpty()) {
            // We add the custom replacement to the database  as reviewed to skip it after the next search in the API
            replacementIndexService.addCustomReviewedReplacement(articleId, options.getSubtype());
        }

        return article;
    }

    @Override
    List<Replacement> findAllReplacements(WikipediaPage article, ArticleReviewOptions options) {
        List<Replacement> replacements = replacementFindService.findCustomReplacements(article.getContent(), options.getSubtype(), options.getSuggestion());

        if (replacements.isEmpty()) {
            // We add the custom replacement to the database  as reviewed to skip it after the next search in the API
            replacementIndexService.addCustomReviewedReplacement(article.getId(), options.getSubtype());
        }

        return replacements;
    }

}
