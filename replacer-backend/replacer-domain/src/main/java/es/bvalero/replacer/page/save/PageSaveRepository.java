package es.bvalero.replacer.page.save;

import es.bvalero.replacer.page.IndexedPage;
import es.bvalero.replacer.page.PageKey;
import java.time.LocalDate;
import java.util.Collection;
import org.jmolecules.architecture.hexagonal.SecondaryPort;

@SecondaryPort
public interface PageSaveRepository {
    /** Save (if needed) a collection of indexed pages and their related replacements */
    void save(Collection<IndexedPage> pages);

    /** Update the date of the last update of the page */
    void updateLastUpdate(PageKey pageKey, LocalDate lastUpdate);

    /** Remove a collection of pages and their related replacements */
    void removeByKey(Collection<PageKey> pageKeys);
}
