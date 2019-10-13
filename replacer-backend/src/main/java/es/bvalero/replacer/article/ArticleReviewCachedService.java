package es.bvalero.replacer.article;

import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

import java.util.List;
import java.util.Optional;
import java.util.Set;

abstract class ArticleReviewCachedService extends ArticleReviewService {

    static final int CACHE_SIZE = 100;

    // Cache the found articles candidates to be reviewed
    // to find faster the next one after the user reviews one
    private SetValuedMap<String, Integer> cachedArticleIds = new HashSetValuedHashMap<>();

    SetValuedMap<String, Integer> getCachedArticleIds() {
        return cachedArticleIds;
    }

    @Override
    Optional<Integer> findArticleIdToReview() {
        // First we try to get the random replacement from the cache
        String key = buildReplacementCacheKey();
        Optional<Integer> articleId = getArticleIdFromCache(key);
        if (!articleId.isPresent()) {
            // We try to find article IDs to review and add them to the cache
            List<Integer> articleIds = findArticleIdsToReview();

            // Return the first result and cache the rest
            articleId = getFirstResultAndCacheTheRest(articleIds, key);
        }

        return articleId;
    }

    abstract String buildReplacementCacheKey();

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

    abstract List<Integer> findArticleIdsToReview();

}
