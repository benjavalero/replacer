package es.bvalero.replacer.page.save;

import es.bvalero.replacer.user.AccessToken;
import es.bvalero.replacer.wikipedia.WikipediaException;
import org.jmolecules.architecture.hexagonal.SecondaryPort;

@SecondaryPort
interface WikipediaPageSaveRepository {
    void save(WikipediaPageSaveCommand pageSave, AccessToken accessToken) throws WikipediaException;
}
