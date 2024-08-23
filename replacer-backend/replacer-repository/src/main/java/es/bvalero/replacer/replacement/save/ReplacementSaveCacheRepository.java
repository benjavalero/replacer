package es.bvalero.replacer.replacement.save;

import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.page.count.PageCountCacheRepository;
import es.bvalero.replacer.replacement.IndexedReplacement;
import java.util.Collection;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Primary
@Transactional
@Repository
class ReplacementSaveCacheRepository implements ReplacementSaveRepository {

    // Dependency injection
    private final ReplacementSaveRepository replacementSaveRepository;
    private final PageCountCacheRepository pageCountCacheRepository;

    ReplacementSaveCacheRepository(
        @Qualifier("replacementSaveJdbcRepository") ReplacementSaveRepository replacementSaveRepository,
        PageCountCacheRepository pageCountCacheRepository
    ) {
        this.replacementSaveRepository = replacementSaveRepository;
        this.pageCountCacheRepository = pageCountCacheRepository;
    }

    @Override
    public void add(Collection<IndexedReplacement> replacements) {
        // The count cache is updated on saving the page
        replacementSaveRepository.add(replacements);
    }

    @Override
    public void update(Collection<IndexedReplacement> replacements) {
        // As we only update the start or the context, there is no point on updating the count cache.
        replacementSaveRepository.update(replacements);
    }

    @Override
    public void remove(Collection<IndexedReplacement> replacements) {
        // The count cache is updated on saving the page
        replacementSaveRepository.remove(replacements);
    }

    @Override
    public void updateReviewerByType(WikipediaLanguage lang, StandardType type, String reviewer) {
        pageCountCacheRepository.removePageCountByType(lang, type);
        replacementSaveRepository.updateReviewerByType(lang, type, reviewer);
    }

    @Override
    public void updateReviewer(Collection<IndexedReplacement> replacements) {
        if (replacements.isEmpty()) {
            return; // Do nothing
        }

        // We can assume all replacements belong to the same page
        final Collection<PageKey> pageKeys = replacements
            .stream()
            .map(IndexedReplacement::getPageKey)
            .collect(Collectors.toUnmodifiableSet());
        assert pageKeys.size() == 1;

        final WikipediaLanguage lang = pageKeys.stream().findAny().orElseThrow(IllegalArgumentException::new).getLang();
        replacements
            .stream()
            .map(IndexedReplacement::getType)
            .distinct()
            .forEach(t -> pageCountCacheRepository.decrementPageCountByType(lang, t));
        replacementSaveRepository.updateReviewer(replacements);
    }

    @Override
    public void removeByType(WikipediaLanguage lang, StandardType type) {
        pageCountCacheRepository.removePageCountByType(lang, type);
        replacementSaveRepository.removeByType(lang, type);
    }
}
