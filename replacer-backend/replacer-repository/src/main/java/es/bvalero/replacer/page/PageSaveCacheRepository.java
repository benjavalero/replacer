package es.bvalero.replacer.page;

import es.bvalero.replacer.common.PageCountDecrementEvent;
import es.bvalero.replacer.common.PageCountIncrementEvent;
import es.bvalero.replacer.common.domain.PageKey;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.StandardType;
import es.bvalero.replacer.replacement.IndexedReplacement;
import es.bvalero.replacer.replacement.IndexedReplacementStatus;
import java.util.Collection;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Primary
@Transactional
@Repository
class PageSaveCacheRepository implements PageSaveRepository {

    // Dependency injection
    private final PageSaveRepository pageSaveRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public PageSaveCacheRepository(
        @Qualifier("pageSaveJdbcRepository") PageSaveRepository pageSaveRepository,
        ApplicationEventPublisher applicationEventPublisher
    ) {
        this.pageSaveRepository = pageSaveRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void save(Collection<IndexedPage> pages) {
        pages.forEach(this::handlePageCache);
        this.pageSaveRepository.save(pages);
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
                    applicationEventPublisher.publishEvent(PageCountIncrementEvent.of(lang, type));
                } else if (toAdd < toRemove) {
                    applicationEventPublisher.publishEvent(PageCountDecrementEvent.of(lang, type));
                }
            }
        }
    }
}
