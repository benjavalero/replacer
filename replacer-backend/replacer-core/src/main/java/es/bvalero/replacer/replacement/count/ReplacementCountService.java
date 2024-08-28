package es.bvalero.replacer.replacement.count;

import es.bvalero.replacer.common.domain.ResultCount;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.page.IndexedPage;
import java.util.Collection;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.stereotype.Service;

@Service
class ReplacementCountService implements ReplacementCountApi {

    @VisibleForTesting
    static final int NUM_RESULTS = 20;

    // Dependency injection
    private final ReplacementCountRepository replacementCountRepository;

    ReplacementCountService(ReplacementCountRepository replacementCountRepository) {
        this.replacementCountRepository = replacementCountRepository;
    }

    @Override
    public int countReviewed(WikipediaLanguage lang) {
        return replacementCountRepository.countReviewed(lang);
    }

    @Override
    public int countNotReviewed(WikipediaLanguage lang) {
        return replacementCountRepository.countNotReviewed(lang);
    }

    @Override
    public Collection<ResultCount<String>> countReviewedGroupedByReviewer(WikipediaLanguage lang) {
        return replacementCountRepository.countReviewedGroupedByReviewer(lang).stream().sorted().toList();
    }

    @Override
    public Collection<ResultCount<IndexedPage>> countNotReviewedGroupedByPage(WikipediaLanguage lang) {
        // For the moment we are not going to cache it as it is used only by admins
        return replacementCountRepository.countNotReviewedGroupedByPage(lang, NUM_RESULTS).stream().sorted().toList();
    }
}
