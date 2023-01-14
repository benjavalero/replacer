package es.bvalero.replacer.wikipedia;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.user.AccessToken;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Primary
@Service
@Profile("!offline")
class WikipediaUserCacheRepository implements WikipediaUserRepository {

    @Autowired
    @Qualifier("wikipediaUserApiRepository")
    private WikipediaUserRepository wikipediaUserRepository;

    // Cache the users which try to access features needing special rights
    // This map can grow. We use Caffeine cache to clean periodically the old or obsolete users.
    private final Cache<WikipediaUserKey, WikipediaUser> cachedUsers = Caffeine
        .newBuilder()
        .expireAfterWrite(1, TimeUnit.DAYS)
        .build();

    @Override
    public Optional<WikipediaUser> findAuthenticatedUser(WikipediaLanguage lang, AccessToken accessToken) {
        return wikipediaUserRepository.findAuthenticatedUser(lang, accessToken);
    }

    @Override
    public Optional<WikipediaUser> findByUsername(WikipediaLanguage lang, String username) {
        return Optional.ofNullable(this.cachedUsers.get(WikipediaUserKey.of(lang, username), this::getUser));
    }

    @Nullable
    private WikipediaUser getUser(WikipediaUserKey userKey) {
        // In case of empty result return a fake user with no groups
        return wikipediaUserRepository.findByUsername(userKey.getLang(), userKey.getUsername()).orElse(null);
    }

    @Value(staticConstructor = "of")
    private static class WikipediaUserKey {

        WikipediaLanguage lang;
        String username;
    }
}
