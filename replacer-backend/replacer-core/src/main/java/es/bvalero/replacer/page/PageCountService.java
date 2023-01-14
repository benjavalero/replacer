package es.bvalero.replacer.page;

import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.ResultCount;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.user.UserRightsService;
import java.util.Collection;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class PageCountService {

    @Autowired
    private UserRightsService userRightsService;

    @Autowired
    private PageCountRepository pageCountRepository;

    public Collection<ResultCount<ReplacementType>> countPagesNotReviewedByType(WikipediaLanguage lang, String user) {
        // Filter the replacement types the user has no rights to see
        return pageCountRepository
            .countPagesNotReviewedByType(lang)
            .stream()
            .map(rc -> ResultCount.of(rc.getKey(), rc.getCount()))
            .filter(rc -> !userRightsService.isTypeForbidden(rc.getKey(), lang, user))
            .collect(Collectors.toUnmodifiableList());
    }
}
