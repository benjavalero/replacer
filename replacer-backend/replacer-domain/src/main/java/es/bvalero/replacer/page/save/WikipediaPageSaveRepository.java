package es.bvalero.replacer.page.save;

import es.bvalero.replacer.common.exception.WikipediaException;
import es.bvalero.replacer.user.AccessToken;
import org.jmolecules.architecture.hexagonal.SecondaryPort;

@SecondaryPort
interface WikipediaPageSaveRepository {
    void save(WikipediaPageSaveCommand pageSave, AccessToken accessToken) throws WikipediaException;
}
