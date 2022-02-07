package es.bvalero.replacer.page.list;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.repository.PageModel;
import es.bvalero.replacer.repository.PageRepository;
import es.bvalero.replacer.repository.ResultCount;
import java.util.Collection;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class PageMostUnreviewedService {

    private static final int NUM_RESULTS = 20;

    @Autowired
    private PageRepository pageRepository;

    Collection<PageCount> countPagesWithMoreReplacementsToReview(WikipediaLanguage lang) {
        return toDto(pageRepository.countPagesWithMoreReplacementsToReview(lang, NUM_RESULTS));
    }

    private Collection<PageCount> toDto(Collection<ResultCount<PageModel>> counts) {
        return counts
            .stream()
            .map(count -> PageCount.of(count.getKey().getPageId(), count.getKey().getTitle(), count.getCount()))
            .collect(Collectors.toUnmodifiableList());
    }
}
