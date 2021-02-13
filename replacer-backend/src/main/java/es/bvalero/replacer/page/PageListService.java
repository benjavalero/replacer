package es.bvalero.replacer.page;

import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.replacement.ReplacementService;
import java.text.Collator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class PageListService {

    @Autowired
    private ReplacementService replacementService;

    List<String> findPageTitlesToReviewBySubtype(WikipediaLanguage lang, String type, String subtype) {
        List<String> titles = replacementService.findPageTitlesToReviewBySubtype(lang, type, subtype);
        titles.sort(Collator.getInstance(FinderUtils.LOCALE_ES));
        return titles;
    }

    void reviewAsSystemBySubtype(WikipediaLanguage lang, String type, String subtype) {
        // These reviewed replacements will be cleaned up in the next dump indexation
        replacementService.reviewAsSystemBySubtype(lang, type, subtype);
    }
}
