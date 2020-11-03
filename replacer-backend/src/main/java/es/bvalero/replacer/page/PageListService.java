package es.bvalero.replacer.page;

import es.bvalero.replacer.finder.FinderUtils;
import es.bvalero.replacer.replacement.ReplacementDao;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.text.Collator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class PageListService {
    @Autowired
    private ReplacementDao replacementDao;

    List<String> findPageTitlesToReviewBySubtype(WikipediaLanguage lang, String type, String subtype) {
        List<String> titles = replacementDao.findPageTitlesToReviewBySubtype(lang, type, subtype);
        titles.sort(Collator.getInstance(FinderUtils.LOCALE_ES));
        return titles;
    }

    void reviewAsSystemBySubtype(WikipediaLanguage lang, String type, String subtype) {
        // These reviewed replacements will be cleaned up in the next dump indexation
        replacementDao.reviewAsSystemBySubtype(lang, type, subtype);
    }
}
