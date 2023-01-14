package es.bvalero.replacer.page;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Setter;
import org.jetbrains.annotations.TestOnly;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Extension of page service to find pages to review in batch to reduce database calls */
@Service
public class PageBatchService extends PageService {

    @Autowired
    private PageRepository pageRepository;

    @Setter(onMethod_ = @TestOnly)
    @Value("${replacer.dump.batch.chunk.size}")
    private int chunkSize;

    // No need to store the lang. We can assume we are not indexing more than one language at a time.
    private final Map<Integer, IndexedPage> pageMap = new HashMap<>(this.chunkSize);

    private int minCachedId = 0;
    private int maxCachedId = 0;

    public Optional<IndexedPage> findPageByKey(PageKey pageKey) {
        // If the page ID is lower than the minimum ID then we are in a new indexing
        if (pageKey.getPageId() < this.minCachedId) {
            resetCache();
        }

        // Load the cache the first time or when needed
        if (this.maxCachedId == 0 || pageKey.getPageId() > this.maxCachedId) {
            clean();

            this.minCachedId = this.maxCachedId + 1;
            while (pageKey.getPageId() > this.maxCachedId) {
                // In case there is a gap greater than the configured chunk size between DB Replacement IDs
                this.maxCachedId += this.chunkSize;
            }
            load(this.minCachedId, this.maxCachedId, pageKey.getLang());
        }

        return Optional.ofNullable(this.pageMap.remove(pageKey.getPageId()));
    }

    private void load(int minId, int maxId, WikipediaLanguage lang) {
        assert this.pageMap.isEmpty();
        pageRepository
            .findPagesByIdInterval(lang, minId, maxId)
            .forEach(page -> this.pageMap.put(page.getPageKey().getPageId(), page));
    }

    private void clean() {
        // Clear the cache if obsolete (we assume the dump pages are in order)
        // The remaining cached pages are not in the dump, so we remove them from DB.
        //  Note that this might not be called for the last pages in a dump until the next indexing starts
        if (!this.pageMap.isEmpty()) {
            removePages(this.pageMap.values());
            this.pageMap.clear();
        }
    }

    private void resetCache() {
        clean();
        this.minCachedId = 0;
        this.maxCachedId = 0;
    }

    private void removePages(Collection<IndexedPage> pages) {
        removePagesByKey(pages.stream().map(IndexedPage::getPageKey).collect(Collectors.toUnmodifiableSet()));
    }
}
