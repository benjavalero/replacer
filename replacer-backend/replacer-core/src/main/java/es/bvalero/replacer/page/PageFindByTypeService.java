package es.bvalero.replacer.page;

import static es.bvalero.replacer.common.util.ReplacerUtils.LOCALE_ES;

import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.text.Collator;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class PageFindByTypeService {

    @Autowired
    private PageRepository pageRepository;

    Collection<String> findPagesToReviewByType(WikipediaLanguage lang, ReplacementType type) {
        return pageRepository
            .findPageTitlesNotReviewedByType(lang, type)
            .stream()
            .filter(Objects::nonNull)
            .sorted(Collator.getInstance(LOCALE_ES))
            .collect(Collectors.toUnmodifiableList());
    }
}
