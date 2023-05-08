package es.bvalero.replacer.replacement;

import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.page.PageCountCacheRepository;
import es.bvalero.replacer.page.PageKey;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Primary
@Transactional
@Repository
class ReplacementSaveCacheRepository implements ReplacementSaveRepository {

    @Autowired
    @Qualifier("replacementJdbcRepository")
    private ReplacementSaveRepository replacementSaveRepository;

    @Autowired
    private PageCountCacheRepository pageCountCacheRepository;

    @Override
    public void add(Collection<IndexedReplacement> replacements) {
        // In case of batch indexing the replacements may belong to several pages
        final Map<PageKey, StandardType> distinctPairs = new HashMap<>();
        replacements.forEach(r -> distinctPairs.put(r.getPageKey(), r.getType()));
        distinctPairs.forEach((pageKey, type) -> pageCountCacheRepository.incrementPageCount(pageKey.getLang(), type));

        replacementSaveRepository.add(replacements);
    }

    @Override
    public void update(Collection<IndexedReplacement> replacements) {
        // As we only update the start or the context, there is no point on updating the count cache.
        replacementSaveRepository.update(replacements);
    }

    @Override
    public void remove(Collection<IndexedReplacement> replacements) {
        // In case of batch indexing the replacements may belong to several pages
        final Map<PageKey, StandardType> distinctPairs = new HashMap<>();
        replacements.forEach(r -> distinctPairs.put(r.getPageKey(), r.getType()));
        distinctPairs.forEach((pageKey, type) -> pageCountCacheRepository.decrementPageCount(pageKey.getLang(), type));

        replacementSaveRepository.remove(replacements);
    }

    @Override
    public void updateReviewerByType(WikipediaLanguage lang, StandardType type, String reviewer) {
        pageCountCacheRepository.removePageCount(lang, type);
        replacementSaveRepository.updateReviewerByType(lang, type, reviewer);
    }

    @Override
    public void updateReviewerByPageAndType(PageKey pageKey, @Nullable StandardType type, String reviewer) {
        if (type != null) {
            pageCountCacheRepository.decrementPageCount(pageKey.getLang(), type);
        }
        // This is just to update the cache, the replacement must have been reindexed and should not exist in DB anymore.
        replacementSaveRepository.updateReviewerByPageAndType(pageKey, type, reviewer);
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
            .forEach(t -> pageCountCacheRepository.decrementPageCount(lang, t));
        replacementSaveRepository.updateReviewer(replacements);
    }

    @Override
    public void removeByType(WikipediaLanguage lang, StandardType type) {
        pageCountCacheRepository.removePageCount(lang, type);
        replacementSaveRepository.removeByType(lang, type);
    }
}
