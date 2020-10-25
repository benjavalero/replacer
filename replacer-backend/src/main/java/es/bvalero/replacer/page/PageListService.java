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

    List<String> findPageList(WikipediaLanguage lang, String type, String subtype) {
        List<String> titles = replacementDao.findPageTitlesByTypeAndSubtype(lang, type, subtype);
        titles.sort(Collator.getInstance(FinderUtils.LOCALE_ES));
        return titles;
    }

    void reviewPagesByTypeAndSubtype(WikipediaLanguage lang, String type, String subtype) {
        replacementDao.reviewTypeReplacementsAsSystem(lang, type, subtype);
    }
}
