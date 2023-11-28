package es.bvalero.replacer.replacement;

import es.bvalero.replacer.common.domain.CustomType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.page.PageKey;
import java.util.Collection;
import org.jmolecules.architecture.hexagonal.SecondaryPort;

@SecondaryPort
interface CustomRepository {
    /** Add a custom replacement */
    void add(IndexedCustomReplacement customReplacement);

    /** Find the pages reviewed for the given custom replacement and return the IDs */
    Collection<PageKey> findPagesReviewed(WikipediaLanguage lang, CustomType type);
}
