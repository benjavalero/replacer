package es.bvalero.replacer.user;

import es.bvalero.replacer.common.domain.AccessToken;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Optional;
import org.jmolecules.architecture.hexagonal.PrimaryPort;

/** Service to retrieve details about the users of the application */
@PrimaryPort
public interface UserApi {
    /** Find a user authenticated in the application */
    Optional<User> findAuthenticatedUser(WikipediaLanguage lang, AccessToken accessToken);
}
