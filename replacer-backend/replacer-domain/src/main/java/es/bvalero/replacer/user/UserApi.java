package es.bvalero.replacer.user;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Optional;

/** Service to retrieve details about the users of the application */
public interface UserApi {
    /** Find a user authenticated in the application */
    Optional<User> findAuthenticatedUser(WikipediaLanguage lang, AccessToken accessToken);
}
