package es.bvalero.replacer.page;

import es.bvalero.replacer.common.domain.PageKey;
import java.util.Collection;
import org.jmolecules.architecture.hexagonal.SecondaryPort;

@SecondaryPort
public interface PageSaveRepository {
    /** Save (if needed) a collection of indexed pages and their related replacements */
    void save(Collection<IndexedPage> pages);

    /** Remove a collection of pages and their related replacements */
    void removeByKey(Collection<PageKey> pageKeys);
}
