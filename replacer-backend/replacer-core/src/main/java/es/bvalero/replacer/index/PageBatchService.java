package es.bvalero.replacer.index;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import es.bvalero.replacer.common.domain.PageKey;
import es.bvalero.replacer.page.IndexedPage;
import es.bvalero.replacer.page.PageRepository;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.Setter;
import org.jetbrains.annotations.TestOnly;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Extension of page service to find indexed pages in batch to reduce database calls.
 * Pages are cached with a TTL so entries not requested by any dump are eventually evicted.
 * Note: this service no longer removes from DB the pages not found in the dump.
 * Those "orphan" pages will be cleaned lazily when accessed during normal application use.
 */
@Service
class PageBatchService {

    // Dependency injection
    private final PageRepository pageRepository;

    @Setter(onMethod_ = @TestOnly)
    @Value("${replacer.dump.batch.chunk.size}")
    private int chunkSize;

    // Thread-safe cache with TTL. Pages not requested will be evicted automatically.
    // We use PageKey (lang + pageId) as key instead of just the pageId
    // to support potential future parallel indexing of multiple languages.
    // The overhead of PageKey vs Integer as key is negligible.
    private final Cache<PageKey, IndexedPage> pageCache = Caffeine.newBuilder()
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .build();

    PageBatchService(PageRepository pageRepository) {
        this.pageRepository = pageRepository;
    }

    Optional<IndexedPage> findByKey(PageKey pageKey) {
        IndexedPage cached = pageCache.getIfPresent(pageKey);
        if (cached != null) {
            pageCache.invalidate(pageKey);
            return Optional.of(cached);
        }

        // Load the range from DB and cache it
        int minId = pageKey.getPageId();
        int maxId = minId + chunkSize - 1;
        pageRepository
            .findByIdRange(pageKey.getLang(), minId, maxId)
            .forEach(page -> pageCache.put(page.getPageKey(), page));

        // Try again after loading
        cached = pageCache.getIfPresent(pageKey);
        if (cached != null) {
            pageCache.invalidate(pageKey);
            return Optional.of(cached);
        }
        return Optional.empty();
    }
}
