package es.bvalero.replacer.wikipedia;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.user.AccessToken;
import es.bvalero.replacer.user.UserId;
import java.util.Optional;

/** Repository to perform operations on Wikipedia users */
public interface WikipediaUserRepository {
    Optional<WikipediaUser> findAuthenticatedUser(WikipediaLanguage lang, AccessToken accessToken);

    Optional<WikipediaUser> findById(UserId userId);
}
