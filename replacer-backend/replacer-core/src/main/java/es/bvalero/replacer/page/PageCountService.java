package es.bvalero.replacer.page;

import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.ResultCount;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.user.UserId;
import es.bvalero.replacer.user.UserRightsService;
import java.util.Collection;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PageCountService {

    @Autowired
    private UserRightsService userRightsService;

    @Autowired
    private PageCountRepository pageCountRepository;

    public Collection<ResultCount<ReplacementType>> countPagesNotReviewedByType(UserId userId) {
        // Filter the replacement types the user has no rights to see
        return pageCountRepository
            .countPagesNotReviewedByType(userId.getLang())
            .stream()
            .map(rc -> ResultCount.of(rc.getKey(), rc.getCount()))
            .filter(rc -> !userRightsService.isTypeForbidden(rc.getKey(), userId))
            .collect(Collectors.toUnmodifiableList());
    }

    public int countPagesToReviewByNoType(WikipediaLanguage lang) {
        return pageCountRepository.countNotReviewedByType(lang, null);
    }

    public int countPagesToReviewByType(WikipediaLanguage lang, ReplacementType type) {
        return pageCountRepository.countNotReviewedByType(lang, type);
    }
}
