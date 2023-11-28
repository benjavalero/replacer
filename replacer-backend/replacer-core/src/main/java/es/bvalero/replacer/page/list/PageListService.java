package es.bvalero.replacer.page.list;

import static es.bvalero.replacer.common.util.ReplacerUtils.LOCALE_ES;
import static es.bvalero.replacer.replacement.IndexedReplacement.REVIEWER_SYSTEM;

import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.page.PageRepository;
import es.bvalero.replacer.replacement.ReplacementSaveRepository;
import java.text.Collator;
import java.util.Collection;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
class PageListService {

    // Dependency injection
    private final PageRepository pageRepository;
    private final ReplacementSaveRepository replacementSaveRepository;

    PageListService(PageRepository pageRepository, ReplacementSaveRepository replacementSaveRepository) {
        this.pageRepository = pageRepository;
        this.replacementSaveRepository = replacementSaveRepository;
    }

    /** Find the pages to review by the given type and return the titles sorted alphabetically */
    Collection<String> findPageTitlesNotReviewedByType(WikipediaLanguage lang, StandardType type) {
        return pageRepository
            .findTitlesNotReviewedByType(lang, type)
            .stream()
            .filter(Objects::nonNull)
            .sorted(Collator.getInstance(LOCALE_ES))
            .toList();
    }

    /** Set as reviewed (by the system) all the replacements of the given type to review */
    void updateSystemReviewerByType(WikipediaLanguage lang, StandardType type) {
        replacementSaveRepository.updateReviewerByType(lang, type, REVIEWER_SYSTEM);
    }
}
