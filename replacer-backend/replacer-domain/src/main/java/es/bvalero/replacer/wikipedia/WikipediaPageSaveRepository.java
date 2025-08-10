package es.bvalero.replacer.wikipedia;

import es.bvalero.replacer.auth.AccessToken;
import org.jmolecules.architecture.hexagonal.SecondaryPort;

@SecondaryPort
public interface WikipediaPageSaveRepository {
    WikipediaPageSaveResult save(WikipediaPageSaveCommand pageSave, AccessToken accessToken) throws WikipediaException;
}
