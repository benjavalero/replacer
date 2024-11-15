package es.bvalero.replacer.page.list;

import static es.bvalero.replacer.common.util.ReplacerUtils.LOCALE_ES;
import static es.bvalero.replacer.replacement.IndexedReplacement.REVIEWER_SYSTEM;

import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.page.find.PageRepository;
import es.bvalero.replacer.replacement.save.ReplacementSaveRepository;
import java.text.Collator;
import java.util.Collection;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
class PageListService implements PageListApi {

    // Dependency injection
    private final PageRepository pageRepository;
    private final ReplacementSaveRepository replacementSaveRepository;

    PageListService(PageRepository pageRepository, ReplacementSaveRepository replacementSaveRepository) {
        this.pageRepository = pageRepository;
        this.replacementSaveRepository = replacementSaveRepository;
    }

    @Override
    public Collection<String> findPageTitlesNotReviewedByType(WikipediaLanguage lang, StandardType type) {
        return pageRepository
            .findTitlesNotReviewedByType(lang, type)
            .stream()
            .filter(Objects::nonNull)
            .sorted(Collator.getInstance(LOCALE_ES))
            .toList();
    }

    @Override
    public void updateSystemReviewerByType(WikipediaLanguage lang, StandardType type) {
        replacementSaveRepository.updateReviewerByType(lang, type, REVIEWER_SYSTEM);
    }
}
