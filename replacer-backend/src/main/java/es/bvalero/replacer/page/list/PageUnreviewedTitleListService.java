package es.bvalero.replacer.page.list;

import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.repository.PageRepository;
import java.text.Collator;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class PageUnreviewedTitleListService {

    @Autowired
    private PageRepository pageRepository;

    Collection<String> findPageTitlesToReviewByType(WikipediaLanguage lang, ReplacementType type) {
        return pageRepository
            .findPageTitlesToReviewByType(lang, type.getKind().getLabel(), type.getSubtype())
            .stream()
            .filter(Objects::nonNull)
            .sorted(Collator.getInstance(FinderUtils.LOCALE_ES))
            .collect(Collectors.toUnmodifiableList());
    }
}
