package es.bvalero.replacer.user;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("offline")
class WikipediaUserOfflineRepository implements WikipediaUserRepository {

    @Override
    public Optional<WikipediaUser> findAuthenticatedUser(WikipediaLanguage lang, AccessToken accessToken) {
        UserId userId = UserId.of(lang, "offline");
        return Optional.of(WikipediaUser.builder().id(userId).autoConfirmed(true).build());
    }
}
