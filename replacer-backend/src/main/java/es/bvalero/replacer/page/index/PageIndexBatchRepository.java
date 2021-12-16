package es.bvalero.replacer.page.index;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaPageId;
import es.bvalero.replacer.page.removeobsolete.RemoveObsoletePageService;
import es.bvalero.replacer.repository.PageIndexRepository;
import es.bvalero.replacer.repository.PageModel;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Setter;
import org.jetbrains.annotations.TestOnly;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Implementation of the page repository which retrieves the pages in batch when indexing */
@Qualifier("pageIndexBatchRepository")
@Transactional
@Repository
class PageIndexBatchRepository implements PageIndexRepository {

    @Qualifier("pageJdbcRepository")
    @Autowired
    private PageIndexRepository pageIndexRepository;

    @Autowired
    private RemoveObsoletePageService removeObsoletePageService;

    @Setter(onMethod_ = @TestOnly)
    @Value("${replacer.dump.batch.chunk.size}")
    private int chunkSize;

    // No need to store the lang. We can assume we are not indexing more than one language at a time.
    private final Map<Integer, PageModel> pageMap = new HashMap<>(chunkSize);

    private int minCachedId = 0;
    private int maxCachedId = 0;

    @Override
    public Optional<PageModel> findPageById(WikipediaPageId id) {
        // If the page ID is lower than the minimum ID then we are in a new indexing
        if (id.getPageId() < minCachedId) {
            resetCache();
        }

        // Load the cache the first time or when needed
        if (maxCachedId == 0 || id.getPageId() > maxCachedId) {
            clean();

            minCachedId = maxCachedId + 1;
            while (id.getPageId() > maxCachedId) {
                // In case there is a gap greater than the configured chunk size between DB Replacement IDs
                maxCachedId += chunkSize;
            }
            load(minCachedId, maxCachedId, id.getLang());
        }

        return Optional.ofNullable(pageMap.remove(id.getPageId()));
    }

    @Override
    public Collection<PageModel> findPagesByIdInterval(WikipediaLanguage lang, int minPageId, int maxPageId) {
        return pageIndexRepository.findPagesByIdInterval(lang, minPageId, maxPageId);
    }

    private void load(int minId, int maxId, WikipediaLanguage lang) {
        assert pageMap.isEmpty();
        this.findPagesByIdInterval(lang, minId, maxId).forEach(page -> pageMap.put(page.getPageId(), page));
    }

    private void clean() {
        // Clear the cache if obsolete (we assume the dump pages are in order)
        // The remaining cached pages are not in the dump, so we remove them from DB.
        //  Note that this might not be called for the last pages in a dump until the next indexing starts
        if (!pageMap.isEmpty()) {
            this.removePages(pageMap.values());
            pageMap.clear();
        }
    }

    private void resetCache() {
        this.clean();
        this.minCachedId = 0;
        this.maxCachedId = 0;
    }

    private void removePages(Collection<PageModel> pages) {
        removeObsoletePageService.removeObsoletePages(
            pages.stream().map(this::toDomain).collect(Collectors.toUnmodifiableSet())
        );
    }

    private WikipediaPageId toDomain(PageModel page) {
        return WikipediaPageId.of(WikipediaLanguage.valueOfCode(page.getLang()), page.getPageId());
    }
}
