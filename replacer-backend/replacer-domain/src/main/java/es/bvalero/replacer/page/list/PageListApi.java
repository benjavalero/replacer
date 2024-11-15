package es.bvalero.replacer.page.list;

import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Collection;
import org.jmolecules.architecture.hexagonal.PrimaryPort;

@PrimaryPort
interface PageListApi {
    /** Find the pages to review by the given type and return the titles sorted alphabetically */
    Collection<String> findPageTitlesNotReviewedByType(WikipediaLanguage lang, StandardType type);

    /** Set as reviewed (by the system) all the replacements of the given type to review */
    void updateSystemReviewerByType(WikipediaLanguage lang, StandardType type);
}
