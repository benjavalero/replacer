package es.bvalero.replacer.page;

import es.bvalero.replacer.common.domain.ResultCount;
import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.user.User;
import java.util.Collection;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PageCountService {

    @Autowired
    private PageCountRepository pageCountRepository;

    public Collection<ResultCount<StandardType>> countPagesNotReviewedByType(User user) {
        // Filter the replacement types the user has no rights to see
        return pageCountRepository
            .countPagesNotReviewedByType(user.getId().getLang())
            .stream()
            .map(rc -> ResultCount.of(rc.getKey(), rc.getCount()))
            .filter(rc -> !rc.getKey().isTypeForbidden(user))
            .collect(Collectors.toUnmodifiableList());
    }

    public int countPagesToReviewByNoType(WikipediaLanguage lang) {
        return pageCountRepository.countNotReviewedByType(lang, null);
    }

    public int countPagesToReviewByType(WikipediaLanguage lang, StandardType type) {
        return pageCountRepository.countNotReviewedByType(lang, type);
    }
}
