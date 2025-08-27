package es.bvalero.replacer.page.list;

import static es.bvalero.replacer.common.util.ReplacerUtils.LOCALE_ES;
import static es.bvalero.replacer.page.IndexedReplacement.REVIEWER_SYSTEM;

import es.bvalero.replacer.common.domain.PageTitle;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.StandardType;
import es.bvalero.replacer.page.PageRepository;
import es.bvalero.replacer.page.PageSaveRepository;
import java.text.Collator;
import java.util.Collection;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
class PageListService implements PageListApi {

    // Dependency injection
    private final PageRepository pageRepository;
    private final PageSaveRepository pageSaveRepository;

    PageListService(PageRepository pageRepository, PageSaveRepository pageSaveRepository) {
        this.pageRepository = pageRepository;
        this.pageSaveRepository = pageSaveRepository;
    }

    @Override
    public Collection<String> findPageTitlesNotReviewedByType(WikipediaLanguage lang, StandardType type) {
        return pageRepository
            .findTitlesNotReviewedByType(lang, type)
            .stream()
            .filter(Objects::nonNull)
            .map(PageTitle::getTitle)
            .sorted(Collator.getInstance(LOCALE_ES))
            .toList();
    }

    @Override
    public void updateSystemReviewerByType(WikipediaLanguage lang, StandardType type) {
        pageSaveRepository.updateReviewerByType(lang, type, REVIEWER_SYSTEM);
    }
}
