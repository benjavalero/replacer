package es.bvalero.replacer.wikipedia;

import es.bvalero.replacer.common.domain.AccessToken;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Optional;
import org.jmolecules.architecture.hexagonal.SecondaryPort;

/** Repository to retrieve a Wikipedia user */
@SecondaryPort
public interface WikipediaUserRepository {
    /** Find a user authenticated in Wikipedia */
    Optional<WikipediaUser> findAuthenticatedUser(WikipediaLanguage lang, AccessToken accessToken);
}
