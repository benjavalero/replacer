package es.bvalero.replacer.page.index.batch;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaPageId;
import es.bvalero.replacer.repository.PageModel;
import es.bvalero.replacer.repository.PageRepository;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.Setter;
import org.jetbrains.annotations.TestOnly;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Helper component to retrieve the pages and replacements in batches
 * and cache temporarily the results in order to reduce the calls to database while dump indexing.
 */
@Component
class PageIndexCacheHelper {

    @Autowired
    private PageRepository pageRepository;

    @Setter(onMethod_ = @TestOnly)
    @Value("${replacer.dump.batch.chunk.size}")
    private int chunkSize;

    // No need to store the lang. We can assume we are not indexing more than one language at a time.
    private final Map<Integer, PageModel> pageMap = new HashMap<>(chunkSize);

    private int maxCachedId = 0;

    Optional<PageModel> findPageById(WikipediaPageId id) {
        // Load the cache the first time or when needed
        if (maxCachedId == 0 || id.getPageId() > maxCachedId) {
            clean();

            int minId = maxCachedId + 1;
            while (id.getPageId() > maxCachedId) {
                // In case there is a gap greater than the configured chunk size between DB Replacement IDs
                maxCachedId += chunkSize;
            }
            load(minId, maxCachedId, id.getLang());
        }

        return Optional.ofNullable(pageMap.remove(id.getPageId()));
    }

    private Collection<PageModel> findPagesByIdInterval(WikipediaLanguage lang, int minPageId, int maxPageId) {
        return pageRepository.findPagesByIdInterval(lang, minPageId, maxPageId);
    }

    private void load(int minId, int maxId, WikipediaLanguage lang) {
        assert pageMap.isEmpty();
        this.findPagesByIdInterval(lang, minId, maxId).forEach(page -> pageMap.put(page.getPageId(), page));
    }

    private void clean() {
        // Clear the cache if obsolete (we assume the dump pages are in order)
        // The remaining cached pages are not in the dump, so we remove them from DB.
        if (!pageMap.isEmpty()) {
            this.removePages(pageMap.values());
            pageMap.clear();
        }
    }

    void resetCache() {
        this.clean();
        this.maxCachedId = 0;
    }

    private void removePages(Collection<PageModel> pages) {
        pageRepository.removePages(pages);
    }
}
