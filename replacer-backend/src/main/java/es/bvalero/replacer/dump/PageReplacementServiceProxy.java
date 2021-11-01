package es.bvalero.replacer.dump;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.domain.WikipediaLanguage;
import es.bvalero.replacer.replacement.ReplacementEntity;
import es.bvalero.replacer.replacement.ReplacementService;
import java.util.ArrayList;
import java.util.List;
import lombok.Setter;
import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
class PageReplacementServiceProxy implements PageReplacementService {

    @Autowired
    private ReplacementService replacementService;

    @Setter
    @Value("${replacer.dump.batch.chunk.size}")
    private int chunkSize;

    // No need to store the lang. We can assume we are not indexing more than one language at a time.
    private final ListValuedMap<Integer, ReplacementEntity> replacementMap = new ArrayListValuedHashMap<>(chunkSize);

    private int maxCachedId = 0;

    @Override
    public List<ReplacementEntity> findByPageId(int pageId, WikipediaLanguage lang) {
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
        for (int obsoleteId : replacementMap.keySet()) {
            replacementService.indexObsoleteByPageId(lang, obsoleteId);
        }
        replacementMap.clear();
    }

    @Loggable(value = Loggable.TRACE)
    @Override
    public void finish(WikipediaLanguage lang) {
        this.clean(lang);
        this.maxCachedId = 0;
    }
}
