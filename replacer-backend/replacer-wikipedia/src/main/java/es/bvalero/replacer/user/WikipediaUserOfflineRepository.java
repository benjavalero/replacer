package es.bvalero.replacer.user;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("offline")
class WikipediaUserOfflineRepository implements WikipediaUserRepository {

    @Override
    public Optional<WikipediaUser> findAuthenticatedUser(WikipediaLanguage lang, AccessToken accessToken) {
        UserId userId = UserId.of(lang, "offline");
        return Optional.of(WikipediaUser.of(userId, true, false));
    }
}
