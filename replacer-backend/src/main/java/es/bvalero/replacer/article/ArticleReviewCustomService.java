package es.bvalero.replacer.article;

import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFinderService;
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
    private ReplacementFinderService replacementFinderService;

    @Autowired
    private ReplacementIndexService replacementIndexService;

    private String replacement;
    private String suggestion;

    Optional<ArticleReview> findRandomArticleReview(String replacement, String suggestion) {
        this.replacement = replacement;
        this.suggestion = suggestion;
        return findRandomArticleReview();
    }

    @Override
    String buildReplacementCacheKey() {
        return String.format("%s-%s", ReplacementFinderService.CUSTOM_FINDER_TYPE, replacement);
    }

    @Override
    List<Integer> findArticleIdsToReview() {
        try {
            List<Integer> articleIds = new ArrayList<>(wikipediaService.getPageIdsByStringMatch(replacement));

            // Check that the replacement has not already been reviewed
            articleIds.removeIf(id -> replacementRepository.countByArticleIdAndTypeAndSubtypeAndReviewerNotNull(
                    id, ReplacementFinderService.CUSTOM_FINDER_TYPE, replacement) > 0);

            return articleIds;
        } catch (WikipediaException e) {
            LOGGER.error("Error searching page IDs from Wikipedia", e);
            return Collections.emptyList();
        }
    }

    Optional<ArticleReview> getArticleReview(int articleId, String replacement, String suggestion) {
        this.replacement = replacement;
        this.suggestion = suggestion;
        return getArticleReview(articleId);
    }

    @Override
    Optional<WikipediaPage> getArticleFromWikipedia(int articleId) {
        Optional<WikipediaPage> article = super.getArticleFromWikipedia(articleId);

        if (!article.isPresent()) {
            // We add the custom replacement to the database  as reviewed to skip it after the next search in the API
            replacementIndexService.addCustomReviewedReplacement(articleId, replacement);
        }

        return article;
    }

    @Override
    List<Replacement> findAllReplacements(WikipediaPage article) {
        List<Replacement> replacements = replacementFinderService.findCustomReplacements(article.getContent(), replacement, suggestion);

        if (replacements.isEmpty()) {
            // We add the custom replacement to the database  as reviewed to skip it after the next search in the API
            replacementIndexService.addCustomReviewedReplacement(article.getId(), replacement);
        }

        return replacements;
    }

}
