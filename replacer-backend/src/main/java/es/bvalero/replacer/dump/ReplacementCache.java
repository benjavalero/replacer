package es.bvalero.replacer.dump;

import es.bvalero.replacer.replacement.ReplacementDao;
import es.bvalero.replacer.replacement.ReplacementEntity;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
class ReplacementCache {
    @Autowired
    private ReplacementDao replacementDao;

    @Setter
    @Value("${replacer.dump.batch.chunk.size}")
    private int chunkSize;

    private int maxCachedId = 0;
    private final ListValuedMap<Integer, ReplacementEntity> replacementMap = new ArrayListValuedHashMap<>(chunkSize);

    List<ReplacementEntity> findByArticleId(int articleId, WikipediaLanguage lang) {
        // Load the cache the first time or when needed
        if (maxCachedId == 0 || articleId > maxCachedId) {
            clean(lang);

            int minId = maxCachedId + 1;
            while (articleId > maxCachedId) {
                // In case there is a gap greater than 1000 (CACHE SIZE) between DB Replacement IDs
                maxCachedId += chunkSize;
            }
            load(minId, maxCachedId, lang);
        }

        // We create a new collection in order not to lose the items after removing the key from the map
        List<ReplacementEntity> replacements = new ArrayList<>(replacementMap.get(articleId));
        replacementMap.remove(articleId); // No need to check if the ID exists

        return replacements;
    }

    private void load(int minId, int maxId, WikipediaLanguage lang) {
        LOGGER.debug("START Load replacements from database to cache. Article ID between {} and {}", minId, maxId);
        replacementDao
            .findByArticles(minId, maxId, lang)
            .forEach(replacement -> replacementMap.put(replacement.getPageId(), replacement));
        LOGGER.debug("END Load replacements from database to cache. Articles cached: {}", replacementMap.size());
    }

    private void clean(WikipediaLanguage lang) {
        // Clear the cache if obsolete (we assume the dump articles are in order)
        // The remaining cached articles are not in the dump so we "remove" them from DB
        Set<Integer> obsoleteIds = new HashSet<>(replacementMap.keySet());
        // If all the replacements of an article are already reviewed by the system
        // there is no need to do it again
        Set<Integer> notReviewedIds = obsoleteIds
            .stream()
            .filter(id -> anyReplacementNotReviewed(replacementMap.get(id)))
            .collect(Collectors.toSet());
        LOGGER.debug("START Delete obsolete and not reviewed articles in DB: {}", notReviewedIds);
        if (!notReviewedIds.isEmpty()) {
            replacementDao.reviewArticlesReplacementsAsSystem(notReviewedIds, lang);
        }
        replacementMap.clear();
        LOGGER.debug("END Delete obsolete and not reviewed articles in DB");
    }

    private boolean anyReplacementNotReviewed(List<ReplacementEntity> list) {
        return list.stream().anyMatch(item -> item.getReviewer() == null);
    }

    public void finish(WikipediaLanguage lang) {
        LOGGER.debug("Finish Replacement Cache. Reset maxCacheId and Clean.");
        this.clean(lang);
        this.maxCachedId = 0;
    }
}
