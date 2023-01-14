package es.bvalero.replacer.wikipedia;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.user.AccessToken;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("offline")
class WikipediaUserOfflineRepository implements WikipediaUserRepository {

    @Override
    public Optional<WikipediaUser> findAuthenticatedUser(WikipediaLanguage lang, AccessToken accessToken) {
        return Optional.of(getOfflineUser());
    }

    @Override
    public Optional<WikipediaUser> findByUsername(WikipediaLanguage lang, String username) {
        return Optional.of(getOfflineUser());
    }

    private WikipediaUser getOfflineUser() {
        return WikipediaUser.of(
            "offline",
            Arrays.stream(WikipediaUserGroup.values()).collect(Collectors.toUnmodifiableList())
        );
    }
}