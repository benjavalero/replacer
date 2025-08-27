package es.bvalero.replacer.page;

import es.bvalero.replacer.common.domain.PageKey;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.StandardType;
import java.util.Collection;
import org.jmolecules.architecture.hexagonal.SecondaryPort;

@SecondaryPort
public interface PageSaveRepository {
    /** Save (if needed) a collection of indexed pages and their related replacements */
    void save(Collection<IndexedPage> pages);

    /** Remove a collection of pages and their related replacements */
    void removeByKey(Collection<PageKey> pageKeys);

    /** Update the reviewer of all the replacements of the given type to review */
    void updateReviewerByType(WikipediaLanguage lang, StandardType type, String reviewer);

    /** Delete all the replacements to review by type */
    void removeByType(WikipediaLanguage lang, StandardType type);
}
