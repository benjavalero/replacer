package es.bvalero.replacer.article;

import es.bvalero.replacer.replacement.ReplacementRepository;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
class ArticleReviewNoTypeService extends ArticleReviewCachedService {

    @Autowired
    private ReplacementRepository replacementRepository;

    @Override
    String buildReplacementCacheKey(ArticleReviewOptions options) {
        return "";
    }

    @Override
    void removeArticleFromCache(int articleId, String cacheKey) {
        // If no type is specified then remove the article ID from all the lists
        List<String> keys = new ArrayList<>(getCachedArticleIds().keySet());
        keys.forEach(key -> getCachedArticleIds().removeMapping(key, articleId));
    }

    @Override
    List<Integer> findArticleIdsToReview(ArticleReviewOptions options) {
        PageRequest pagination = PageRequest.of(0, CACHE_SIZE);
        // TODO: Receive language as a parameter
        return replacementRepository.findRandomArticleIdsToReview(WikipediaLanguage.SPANISH.getCode(), pagination);
    }

}
