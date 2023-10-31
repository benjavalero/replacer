package es.bvalero.replacer.page;

import static es.bvalero.replacer.common.util.ReplacerUtils.LOCALE_ES;

import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.text.Collator;
import java.util.Collection;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
class PageFindByTypeService {

    // Dependency injection
    private final PageRepository pageRepository;

    PageFindByTypeService(PageRepository pageRepository) {
        this.pageRepository = pageRepository;
    }

    Collection<String> findPagesToReviewByType(WikipediaLanguage lang, StandardType type) {
        return pageRepository
            .findPageTitlesNotReviewedByType(lang, type)
            .stream()
            .filter(Objects::nonNull)
            .sorted(Collator.getInstance(LOCALE_ES))
            .toList();
    }
}
