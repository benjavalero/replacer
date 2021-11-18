package es.bvalero.replacer.page.repository;

import es.bvalero.replacer.domain.WikipediaLanguage;
import java.util.*;
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

    @Setter(AccessLevel.PACKAGE) // For testing
    @Value("${replacer.dump.batch.chunk.size}")
    private int chunkSize;

    // No need to store the lang. We can assume we are not indexing more than one language at a time.
    private final Map<Integer, IndexablePage> pageMap = new HashMap<>(chunkSize);

    private int maxCachedId = 0;

    @Override
    public Optional<IndexablePage> findByPageId(IndexablePageId id) {
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

    @Override
    public List<IndexablePage> findByPageIdInterval(WikipediaLanguage lang, int minPageId, int maxPageId) {
        return indexablePageRepository.findByPageIdInterval(lang, minPageId, maxPageId);
    }

    private void load(int minId, int maxId, WikipediaLanguage lang) {
        assert pageMap.isEmpty();
        this.findByPageIdInterval(lang, minId, maxId).forEach(page -> pageMap.put(page.getPageId(), page));
    }

    private void clean() {
        // Clear the cache if obsolete (we assume the dump pages are in order)
        // The remaining cached pages are not in the dump, so we remove them from DB.
        if (!pageMap.isEmpty()) {
            this.deletePages(pageMap.values());
            pageMap.clear();
        }
    }

    @Override
    public void resetCache() {
        this.clean();
        this.maxCachedId = 0;
    }

    @Override
    public void updatePageTitles(Collection<IndexablePage> pages) {
        indexablePageRepository.updatePageTitles(pages);
    }

    @Override
    public void insertPages(Collection<IndexablePage> pages) {
        indexablePageRepository.insertPages(pages);
    }

    @Override
    public void deletePages(Collection<IndexablePage> pages) {
        indexablePageRepository.deletePages(pages);
    }

    @Override
    public void insertReplacements(Collection<IndexableReplacement> replacements) {
        indexablePageRepository.insertReplacements(replacements);
    }

    @Override
    public void updateReplacements(Collection<IndexableReplacement> replacements) {
        indexablePageRepository.updateReplacements(replacements);
    }

    @Override
    public void deleteReplacements(Collection<IndexableReplacement> replacements) {
        indexablePageRepository.deleteReplacements(replacements);
    }
}
