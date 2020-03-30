package es.bvalero.replacer.article;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
abstract class ArticleReviewCachedService extends ArticleReviewService {

    static final int CACHE_SIZE = 100;

    // Cache the found articles candidates to be reviewed
    // to find faster the next one after the user reviews one
    private SetValuedMap<String, Integer> cachedArticleIds = new HashSetValuedHashMap<>();

    SetValuedMap<String, Integer> getCachedArticleIds() {
        return cachedArticleIds;
    }

    @Override
    Optional<Integer> findArticleIdToReview(ArticleReviewOptions options) {
        LOGGER.info("START Find random article ID...");
        // First we try to get the random replacement from the cache
        String key = buildReplacementCacheKey(options);
        Optional<Integer> articleId = getArticleIdFromCache(key);
        if (articleId.isEmpty()) {
            // We try to find article IDs to review and add them to the cache
            List<Integer> articleIds = findArticleIdsToReview(options);

            // Return the first result and cache the rest
            articleId = getFirstResultAndCacheTheRest(articleIds, key);
        }

        LOGGER.info("END Found random article: {}", articleId.orElse(null));
        return articleId;
    }

    String buildReplacementCacheKey(ArticleReviewOptions options) {
        return String.format("%s-%s", options.getType(), options.getSubtype());
    }

    private Optional<Integer> getArticleIdFromCache(String key) {
        if (cachedArticleIds.containsKey(key)) {
            Set<Integer> randomArticleIds = cachedArticleIds.get(key);
            Optional<Integer> randomArticleId = randomArticleIds.stream().findFirst();
            randomArticleId.ifPresent(id -> removeArticleFromCache(id, key));
            return randomArticleId;
        }
        return Optional.empty();
    }

    private Optional<Integer> getFirstResultAndCacheTheRest(List<Integer> articleIds, String cacheKey) {
        Optional<Integer> firstResult = articleIds.stream().findAny();
        cachedArticleIds.putAll(cacheKey, articleIds);
        firstResult.ifPresent(id -> removeArticleFromCache(id, cacheKey));
        return firstResult;
    }

    void removeArticleFromCache(int articleId, String cacheKey) {
        cachedArticleIds.removeMapping(cacheKey, articleId);
    }

    abstract List<Integer> findArticleIdsToReview(ArticleReviewOptions options);

}
