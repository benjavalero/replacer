package es.bvalero.replacer.replacement;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.page.PageKey;
import java.util.Collection;

interface CustomRepository {
    /** Add a custom replacement */
    void add(IndexedCustomReplacement customReplacement);

    /** Find the pages reviewed for the given custom replacement and return the IDs */
    Collection<PageKey> findPagesReviewed(WikipediaLanguage lang, String replacement, boolean caseSensitive);
}
