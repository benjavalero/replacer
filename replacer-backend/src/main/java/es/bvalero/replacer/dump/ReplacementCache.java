package es.bvalero.replacer.dump;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.replacement.ReplacementEntity;
import es.bvalero.replacer.replacement.ReplacementService;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Setter;
import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class ReplacementCache {

    @Autowired
    private ReplacementService replacementService;

    @Setter
    @Value("${replacer.dump.batch.chunk.size}")
    private int chunkSize;

    private final ListValuedMap<Integer, ReplacementEntity> replacementMap = new ArrayListValuedHashMap<>(chunkSize);
    private int maxCachedId = 0;

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

    @Loggable(prepend = true, value = Loggable.TRACE)
    private void load(int minId, int maxId, WikipediaLanguage lang) {
        replacementService
            .findByPageInterval(minId, maxId, lang)
            .forEach(replacement -> replacementMap.put(replacement.getPageId(), replacement));
    }

    @Loggable(prepend = true, value = Loggable.TRACE)
    private void clean(WikipediaLanguage lang) {
        // Clear the cache if obsolete (we assume the dump pages are in order)
        // The remaining cached pages are not in the dump so we remove them from DB
        Set<Integer> obsoleteIds = new HashSet<>(replacementMap.keySet());

        // We can assume the article in DB doesn't exist anymore
        // In case the article is new (and thus more recent than the dump)
        // we can simplify and also remove it as it will be indexed in the next dump

        // We keep the rows reviewed by any user for the sake of statistics
        if (!obsoleteIds.isEmpty()) {
            replacementService.deleteObsoleteByPageId(lang, obsoleteIds);
        }
        replacementMap.clear();
    }

    @Loggable(value = Loggable.TRACE)
    void finish(WikipediaLanguage lang) {
        this.clean(lang);
        this.maxCachedId = 0;
    }
}
