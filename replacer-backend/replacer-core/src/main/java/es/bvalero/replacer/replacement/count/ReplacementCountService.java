package es.bvalero.replacer.replacement.count;

import es.bvalero.replacer.common.domain.PageTitle;
import es.bvalero.replacer.common.domain.ResultCount;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Collection;
import org.springframework.stereotype.Service;

@Service
class ReplacementCountService implements ReplacementCountApi {

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
    public Collection<ResultCount<PageTitle>> countNotReviewedGroupedByPage(WikipediaLanguage lang) {
        return replacementCountRepository.countNotReviewedGroupedByPage(lang, NUM_RESULTS).stream().sorted().toList();
    }
}
