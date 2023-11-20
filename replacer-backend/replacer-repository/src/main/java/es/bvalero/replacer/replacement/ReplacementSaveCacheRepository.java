package es.bvalero.replacer.replacement;

import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.page.count.PageCountRepository;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Primary
@Transactional
@Repository
class ReplacementSaveCacheRepository implements ReplacementSaveRepository {

    // Dependency injection
    private final ReplacementSaveRepository replacementSaveRepository;
    private final PageCountRepository pageCountRepository;

    ReplacementSaveCacheRepository(
        @Qualifier("replacementJdbcRepository") ReplacementSaveRepository replacementSaveRepository,
        PageCountRepository pageCountRepository
    ) {
        this.replacementSaveRepository = replacementSaveRepository;
        this.pageCountRepository = pageCountRepository;
    }

    @Override
    public void add(Collection<IndexedReplacement> replacements) {
        replacementSaveRepository.add(replacements);
    }

    @Override
    public void update(Collection<IndexedReplacement> replacements) {
        // As we only update the start or the context, there is no point on updating the count cache.
        replacementSaveRepository.update(replacements);
    }

    @Override
    public void remove(Collection<IndexedReplacement> replacements) {
        replacementSaveRepository.remove(replacements);
    }

    @Override
    public void updateReviewerByType(WikipediaLanguage lang, StandardType type, String reviewer) {
        pageCountRepository.removePageCountByType(lang, type);
        replacementSaveRepository.updateReviewerByType(lang, type, reviewer);
    }

    @Override
    public void updateReviewer(Collection<IndexedReplacement> replacements) {
        if (replacements.isEmpty()) {
            return; // Do nothing
        }

        // We can assume all replacements belong to the same page
        final WikipediaLanguage lang = replacements
            .stream()
            .map(r -> r.getPageKey().getLang())
            .findAny()
            .orElseThrow(IllegalArgumentException::new);
        replacements
            .stream()
            .map(IndexedReplacement::getType)
            .distinct()
            .forEach(t -> pageCountRepository.decrementPageCountByType(lang, t));
        replacementSaveRepository.updateReviewer(replacements);
    }

    @Override
    public void removeByType(WikipediaLanguage lang, StandardType type) {
        pageCountRepository.removePageCountByType(lang, type);
        replacementSaveRepository.removeByType(lang, type);
    }
}
