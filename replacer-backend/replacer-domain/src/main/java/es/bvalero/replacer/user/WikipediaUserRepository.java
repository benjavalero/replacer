package es.bvalero.replacer.user;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Optional;
import org.jmolecules.architecture.hexagonal.SecondaryPort;

/** Repository to retrieve a Wikipedia user */
@SecondaryPort
interface WikipediaUserRepository {
    /**
     * Find a user authenticated in Wikipedia.
     * Note that we are returning an application user.
     * The application permissions derived from the Wikipedia groups are calculated here.
     * The other application permissions, like the administration role, have to be decorated later.
     */
    Optional<User> findAuthenticatedUser(WikipediaLanguage lang, AccessToken accessToken);
}
