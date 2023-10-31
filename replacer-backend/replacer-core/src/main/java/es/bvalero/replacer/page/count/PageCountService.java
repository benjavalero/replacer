package es.bvalero.replacer.page.count;

import es.bvalero.replacer.common.domain.ResultCount;
import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.user.User;
import java.util.Collection;
import org.springframework.stereotype.Service;

@Service
public class PageCountService {

    // Dependency injection
    private final PageCountRepository pageCountRepository;

    public PageCountService(PageCountRepository pageCountRepository) {
        this.pageCountRepository = pageCountRepository;
    }

    /** Count the number of pages to review grouped by replacement type */
    public Collection<ResultCount<StandardType>> countNotReviewedGroupedByType(User user) {
        // Filter the replacement types the user has no rights to see
        return pageCountRepository
            .countNotReviewedGroupedByType(user.getId().getLang())
            .stream()
            .map(rc -> ResultCount.of(rc.getKey(), rc.getCount()))
            .filter(rc -> !rc.getKey().isTypeForbidden(user))
            .toList();
    }

    /** Count the number of pages to review */
    public int countNotReviewedByNoType(WikipediaLanguage lang) {
        return pageCountRepository.countNotReviewedByType(lang, null);
    }

    /** Count the number of pages to review by type */
    public int countNotReviewedByType(WikipediaLanguage lang, StandardType type) {
        return pageCountRepository.countNotReviewedByType(lang, type);
    }
}
