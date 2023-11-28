package es.bvalero.replacer.page.count;

import es.bvalero.replacer.common.domain.ResultCount;
import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.user.User;
import java.util.Collection;
import org.springframework.stereotype.Service;

@Service
class PageCountService {

    // Dependency injection
    private final PageCountRepository pageCountRepository;

    PageCountService(PageCountRepository pageCountRepository) {
        this.pageCountRepository = pageCountRepository;
    }

    /** Count the number of pages to review grouped by replacement type */
    Collection<ResultCount<StandardType>> countNotReviewedGroupedByType(User user) {
        // Filter the replacement types the user has no rights to see
        return pageCountRepository
            .countNotReviewedGroupedByType(user.getId().getLang())
            .stream()
            .map(rc -> ResultCount.of(rc.getKey(), rc.getCount()))
            .filter(rc -> !rc.getKey().isTypeForbidden(user))
            .toList();
    }
}
