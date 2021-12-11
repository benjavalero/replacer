package es.bvalero.replacer.page.list;

import static es.bvalero.replacer.repository.ReplacementRepository.REVIEWER_SYSTEM;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.repository.PageRepository;
import es.bvalero.replacer.repository.ReplacementRepository;
import java.text.Collator;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class PageListService {

    // TODO: Replace the type-subtype arguments by the domain ReplacementType

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private ReplacementRepository replacementRepository;

    Collection<String> findPageTitlesToReviewByType(WikipediaLanguage lang, String type, String subtype) {
        return pageRepository
            .findPageTitlesToReviewByType(lang, type, subtype)
            .stream()
            .filter(Objects::nonNull)
            .sorted(Collator.getInstance(FinderUtils.LOCALE_ES))
            .collect(Collectors.toUnmodifiableList());
    }

    void reviewAsSystemByType(WikipediaLanguage lang, String type, String subtype) {
        // These reviewed replacements will be cleaned up in the next dump indexing
        replacementRepository.updateReviewerByType(lang, type, subtype, REVIEWER_SYSTEM);
    }
}
