package es.bvalero.replacer.page.repository;

import es.bvalero.replacer.domain.WikipediaLanguage;
import es.bvalero.replacer.replacement.ReplacementService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Implementation of the indexable page repository which retrieves the pages and replacements in batches
 * and caches temporarily the results in order to reduce the calls to database while dump indexing.
 */
@Component
@Qualifier("indexablePageCacheRepository")
class IndexablePageCacheRepository implements IndexablePageRepository {

    @Autowired
    @Qualifier("indexablePageJdbcRepository")
    private IndexablePageRepository indexablePageRepository;

    @Autowired
    private ReplacementService replacementService;

    @Setter(AccessLevel.PACKAGE) // For testing
    @Value("${replacer.dump.batch.chunk.size}")
    private int chunkSize;

    // No need to store the lang. We can assume we are not indexing more than one language at a time.
    private final Map<Integer, IndexablePageDB> pageMap = new HashMap<>(chunkSize);

    private int maxCachedId = 0;

    @Override
    public Optional<IndexablePageDB> findByPageId(WikipediaLanguage lang, int pageId) {
        // Load the cache the first time or when needed
        if (maxCachedId == 0 || pageId > maxCachedId) {
            clean(lang);

            int minId = maxCachedId + 1;
            while (pageId > maxCachedId) {
                // In case there is a gap greater than the configured chunk size between DB Replacement IDs
                maxCachedId += chunkSize;
            }
            load(minId, maxCachedId, lang);
        }

        return Optional.ofNullable(pageMap.remove(pageId));
    }

    @Override
    public List<IndexablePageDB> findByPageIdInterval(WikipediaLanguage lang, int minPageId, int maxPageId) {
        return indexablePageRepository.findByPageIdInterval(lang, minPageId, maxPageId);
    }

    private void load(int minId, int maxId, WikipediaLanguage lang) {
        assert pageMap.isEmpty();
        this.findByPageIdInterval(lang, minId, maxId).forEach(page -> pageMap.put(page.getId(), page));
    }

    private void clean(WikipediaLanguage lang) {
        // Clear the cache if obsolete (we assume the dump pages are in order)
        // The remaining cached pages are not in the dump, so we remove them from DB.
        for (int obsoleteId : pageMap.keySet()) {
            replacementService.indexObsoleteByPageId(lang, obsoleteId);
        }
        pageMap.clear();
    }

    @Override
    public void resetCache(WikipediaLanguage lang) {
        this.clean(lang);
        this.maxCachedId = 0;
    }
}
