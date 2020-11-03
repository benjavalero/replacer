package es.bvalero.replacer.dump;

import es.bvalero.replacer.replacement.ReplacementDao;
import es.bvalero.replacer.replacement.ReplacementEntity;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    List<ReplacementEntity> findByPageId(int pageId, WikipediaLanguage lang) {
        // Load the cache the first time or when needed
        if (maxCachedId == 0 || pageId > maxCachedId) {
            clean(lang);

            int minId = maxCachedId + 1;
            while (pageId > maxCachedId) {
                // In case there is a gap greater than 1000 (CACHE SIZE) between DB Replacement IDs
                maxCachedId += chunkSize;
            }
            load(minId, maxCachedId, lang);
        }

        // We create a new collection in order not to lose the items after removing the key from the map
        List<ReplacementEntity> replacements = new ArrayList<>(replacementMap.get(pageId));
        replacementMap.remove(pageId); // No need to check if the ID exists

        return replacements;
    }

    private void load(int minId, int maxId, WikipediaLanguage lang) {
        LOGGER.debug("START Load replacements from database to cache. Page ID between {} and {}", minId, maxId);
        replacementDao
            .findByPageInterval(minId, maxId, lang)
            .forEach(replacement -> replacementMap.put(replacement.getPageId(), replacement));
        LOGGER.debug("END Load replacements from database to cache. Pages cached: {}", replacementMap.size());
    }

    private void clean(WikipediaLanguage lang) {
        // Clear the cache if obsolete (we assume the dump pages are in order)
        // The remaining cached pages are not in the dump so we remove them from DB
        Set<Integer> obsoleteIds = new HashSet<>(replacementMap.keySet());

        // We can assume the article in DB doesn't exist anymore
        // In case the article is new (and thus more recent than the dump)
        // we can simplify and also remove it as it will be indexed in the next dump

        // We keep the rows reviewed by any user for the sake of statistics
        LOGGER.debug("START Delete obsolete and not reviewed pages in DB: {}", obsoleteIds);
        if (!obsoleteIds.isEmpty()) {
            replacementDao.deleteObsoleteByPageId(lang, obsoleteIds);
        }
        replacementMap.clear();
        LOGGER.debug("END Delete obsolete and not reviewed pages in DB");
    }

    public void finish(WikipediaLanguage lang) {
        LOGGER.debug("Finish Replacement Cache. Reset maxCacheId and Clean.");
        this.clean(lang);
        this.maxCachedId = 0;
    }
}
