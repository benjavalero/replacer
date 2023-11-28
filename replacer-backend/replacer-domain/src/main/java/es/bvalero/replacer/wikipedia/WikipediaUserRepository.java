package es.bvalero.replacer.wikipedia;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.user.AccessToken;
import es.bvalero.replacer.user.UserId;
import java.util.Optional;
import org.jmolecules.architecture.hexagonal.SecondaryPort;

/** Repository to perform operations on Wikipedia users */
@SecondaryPort
public interface WikipediaUserRepository {
    Optional<WikipediaUser> findAuthenticatedUser(WikipediaLanguage lang, AccessToken accessToken);

    Optional<WikipediaUser> findById(UserId userId);
}
