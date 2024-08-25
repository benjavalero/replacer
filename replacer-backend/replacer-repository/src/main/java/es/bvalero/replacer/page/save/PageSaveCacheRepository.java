package es.bvalero.replacer.page.save;

import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.page.IndexedPage;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.page.count.PageCountCacheRepository;
import es.bvalero.replacer.replacement.IndexedReplacement;
import es.bvalero.replacer.replacement.save.IndexedReplacementStatus;
import java.time.LocalDate;
import java.util.Collection;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Primary
@Transactional
@Repository
class PageSaveCacheRepository implements PageSaveRepository {

    // Dependency injection
    private final PageSaveRepository pageSaveRepository;
    private final PageCountCacheRepository pageCountCacheRepository;

    public PageSaveCacheRepository(
        @Qualifier("pageSaveJdbcRepository") PageSaveRepository pageSaveRepository,
        PageCountCacheRepository pageCountCacheRepository
    ) {
        this.pageSaveRepository = pageSaveRepository;
        this.pageCountCacheRepository = pageCountCacheRepository;
    }

    @Override
    public void save(Collection<IndexedPage> pages) {
        pages.forEach(this::handlePageCache);
        this.pageSaveRepository.save(pages);
    }

    @Override
    public void updateLastUpdate(PageKey pageKey, LocalDate lastUpdate) {
        this.pageSaveRepository.updateLastUpdate(pageKey, lastUpdate);
    }

    @Override
    public void removeByKey(Collection<PageKey> pageKeys) {
        // Not worth to find the replacements in order to update the count cache
        this.pageSaveRepository.removeByKey(pageKeys);
    }

    private void handlePageCache(IndexedPage page) {
        WikipediaLanguage lang = page.getPageKey().getLang();
        Collection<StandardType> types = page
            .getReplacements()
            .stream()
            .map(IndexedReplacement::getType)
            .collect(Collectors.toUnmodifiableSet());

        // Check for each type if it is new or obsolete in the page
        for (StandardType type : types) {
            Collection<IndexedReplacement> unreviewed = page
                .getReplacements()
                .stream()
                .filter(r -> r.getType().equals(type))
                .filter(IndexedReplacement::isToBeReviewed)
                .collect(Collectors.toUnmodifiableSet());
            long toAdd = unreviewed.stream().filter(r -> r.getStatus() == IndexedReplacementStatus.ADD).count();
            long toRemove = unreviewed.stream().filter(r -> r.getStatus() == IndexedReplacementStatus.REMOVE).count();
            long alreadyIndexed = unreviewed.size() - (toAdd + toRemove);
            if (alreadyIndexed == 0) {
                // If we are keeping any replacement, the count doesn't change.
                if (toAdd > toRemove) {
                    pageCountCacheRepository.incrementPageCountByType(lang, type);
                } else if (toAdd < toRemove) {
                    pageCountCacheRepository.decrementPageCountByType(lang, type);
                }
            }
        }
    }
}
