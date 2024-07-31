package es.bvalero.replacer.user;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Slf4j
@Primary
@Service
@Profile("!offline")
class WikipediaUserCacheRepository implements WikipediaUserRepository {

    // Dependency injection
    private final WikipediaUserRepository wikipediaUserRepository;

    // Cache the users which try to access features needing special rights
    // This map can grow. We use Caffeine cache to clean periodically the old or obsolete users.
    private final Cache<AccessToken, WikipediaUser> cachedUsers = Caffeine.newBuilder()
        .removalListener((key, value, cause) -> {
            if (cause.wasEvicted() && value != null) {
                LOGGER.info("Evicted user: {}", ((WikipediaUser) value).getId());
            }
        })
        .expireAfterWrite(1, TimeUnit.DAYS)
        .build();

    WikipediaUserCacheRepository(
        @Qualifier("wikipediaUserApiRepository") WikipediaUserRepository wikipediaUserRepository
    ) {
        this.wikipediaUserRepository = wikipediaUserRepository;
    }

    @Override
    public Optional<WikipediaUser> findAuthenticatedUser(WikipediaLanguage lang, AccessToken accessToken) {
        return Optional.ofNullable(this.cachedUsers.get(accessToken, token -> getAuthenticatedUser(lang, token)));
    }

    @Nullable
    private WikipediaUser getAuthenticatedUser(WikipediaLanguage lang, AccessToken accessToken) {
        // In case of empty result return a fake user with no groups
        return wikipediaUserRepository.findAuthenticatedUser(lang, accessToken).orElse(null);
    }
}
