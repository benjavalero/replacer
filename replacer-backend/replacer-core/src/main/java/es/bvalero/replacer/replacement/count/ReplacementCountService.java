package es.bvalero.replacer.replacement.count;

import es.bvalero.replacer.common.domain.PageTitle;
import es.bvalero.replacer.common.domain.ResultCount;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.CustomType;
import es.bvalero.replacer.replacement.CustomRepository;
import java.util.Collection;
import org.springframework.stereotype.Service;

@Service
class ReplacementCountService implements ReplacementCountApi {

    // Dependency injection
    private final ReplacementCountRepository replacementCountRepository;
    private final CustomRepository customRepository;

    ReplacementCountService(ReplacementCountRepository replacementCountRepository, CustomRepository customRepository) {
        this.replacementCountRepository = replacementCountRepository;
        this.customRepository = customRepository;
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

    @Override
    public Collection<ResultCount<CustomType>> countReviewedGroupedByCustomType(WikipediaLanguage lang) {
        return customRepository.countReviewedGroupedByType(lang, NUM_RESULTS);
    }
}
