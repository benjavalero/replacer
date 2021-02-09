package es.bvalero.replacer.dump;

import es.bvalero.replacer.replacement.ReplacementEntity;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.util.List;

interface PageReplacementService {
    /** Find all the replacements for a given page */
    List<ReplacementEntity> findByPageId(int pageId, WikipediaLanguage lang);

    void finish(WikipediaLanguage lang);
}
