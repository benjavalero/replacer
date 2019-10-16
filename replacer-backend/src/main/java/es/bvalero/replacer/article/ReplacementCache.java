package es.bvalero.replacer.article;

import es.bvalero.replacer.replacement.ReplacementEntity;
import es.bvalero.replacer.replacement.ReplacementIndexService;
import es.bvalero.replacer.replacement.ReplacementRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class ReplacementCache {
    private static final int CACHE_SIZE = 1000;
    private int maxCachedId;
    private ListValuedMap<Integer, ReplacementEntity> replacementMap = new ArrayListValuedHashMap<>(CACHE_SIZE);

    @Autowired
    private ReplacementRepository replacementRepository;

    @Autowired
    private ReplacementIndexService replacementIndexService;

    public List<ReplacementEntity> findByArticleId(int articleId) {
        // Load the cache the first time or when needed
        if (maxCachedId == 0 || articleId > maxCachedId) {
            clean();

            int minId = maxCachedId + 1;
            while (articleId > maxCachedId) {
                // In case there is a gap greater than 1000 (CACHE SIZE) between DB Replacement IDs
                maxCachedId += CACHE_SIZE;
            }
            load(minId, maxCachedId);
        }

        // We create a new collection in order not to lose the items after removing the key from the map
        List<ReplacementEntity> replacements = new ArrayList<>(replacementMap.get(articleId));
        replacementMap.remove(articleId); // No need to check if the ID exists

        return replacements;
    }

    private void load(int minId, int maxId) {
        LOGGER.debug("START Load replacements from database to cache. Article ID between {} and {}", minId, maxId);
        replacementRepository.findByArticles(minId, maxId).forEach(
                replacement -> replacementMap.put(replacement.getArticleId(), replacement));
        LOGGER.debug("END Load replacements from database to cache. Articles cached: {}", replacementMap.size());
    }

    public void clean() {
        // Clear the cache if obsolete (we assume the dump articles are in order)
        // The remaining cached articles are not in the dump so we remove them from DB
        Set<Integer> obsoleteIds = new HashSet<>(replacementMap.keySet());
        LOGGER.debug("START Delete obsolete articles in DB: {}", obsoleteIds);
        replacementIndexService.reviewArticlesReplacementsAsSystem(obsoleteIds);
        replacementMap.clear();
        LOGGER.debug("END Delete obsolete articles in DB");
    }

}
