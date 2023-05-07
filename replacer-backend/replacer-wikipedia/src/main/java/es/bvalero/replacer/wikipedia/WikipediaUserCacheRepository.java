package es.bvalero.replacer.wikipedia;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.user.AccessToken;
import es.bvalero.replacer.user.UserId;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
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
    private final Cache<AccessToken, WikipediaUser> cachedUsers = Caffeine
        .newBuilder()
        .expireAfterWrite(1, TimeUnit.DAYS)
        .build();

    @Override
    public Optional<WikipediaUser> findAuthenticatedUser(WikipediaLanguage lang, AccessToken accessToken) {
        return Optional.ofNullable(this.cachedUsers.get(accessToken, token -> getAuthenticatedUser(lang, token)));
    }

    @Override
    public Optional<WikipediaUser> findById(UserId userId) {
        return wikipediaUserRepository.findById(userId);
    }

    @Nullable
    private WikipediaUser getAuthenticatedUser(WikipediaLanguage lang, AccessToken accessToken) {
        // In case of empty result return a fake user with no groups
        return wikipediaUserRepository.findAuthenticatedUser(lang, accessToken).orElse(null);
    }
}
