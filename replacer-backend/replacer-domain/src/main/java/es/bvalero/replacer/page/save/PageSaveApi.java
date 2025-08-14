package es.bvalero.replacer.page.save;

import es.bvalero.replacer.common.domain.User;
import es.bvalero.replacer.wikipedia.WikipediaException;
import org.jmolecules.architecture.hexagonal.PrimaryPort;

@PrimaryPort
interface PageSaveApi {
    /** Save a reviewed page with or without changes */
    void save(ReviewedPage reviewedPage, User user) throws WikipediaException;
}
