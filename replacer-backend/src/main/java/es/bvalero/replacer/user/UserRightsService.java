package es.bvalero.replacer.user;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import es.bvalero.replacer.common.domain.ReplacerUser;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ForbiddenException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Service to check the rights of a user to use some services of the application */
@Slf4j
@Service
public class UserRightsService {

    @Autowired
    private UserService userService;

    // Cache the users which try to access features needing special rights
    // This map can grow. We use Caffeine cache to clean periodically the old or obsolete users.
    private final Cache<String, ReplacerUser> cachedUsers = Caffeine
        .newBuilder()
        .expireAfterWrite(1, TimeUnit.DAYS)
        .build();

    public void validateAdminUser(WikipediaLanguage lang, String user) throws ForbiddenException {
        if (!isAdmin(lang, user)) {
            LOGGER.error("Unauthorized admin user: {} - {}", lang, user);
            throw new ForbiddenException();
        }
    }

    @VisibleForTesting
    boolean isAdmin(WikipediaLanguage lang, String username) {
        return getCachedUser(lang, username).isAdmin();
    }

    public void validateBotUser(WikipediaLanguage lang, String user) throws ForbiddenException {
        if (!isBot(lang, user)) {
            LOGGER.error("Unauthorized bot user: {} - {}", lang, user);
            throw new ForbiddenException();
        }
    }

    public boolean isBot(WikipediaLanguage lang, String username) {
        return getCachedUser(lang, username).isBot();
    }

    private ReplacerUser getCachedUser(WikipediaLanguage lang, String username) {
        return Objects.requireNonNull(cachedUsers.get(buildCacheKey(lang, username), this::getUser));
    }

    private String buildCacheKey(WikipediaLanguage lang, String username) {
        return String.format("%s-%s", lang.getCode(), username);
    }

    private ReplacerUser getUser(String cacheKey) {
        String[] tokens = cacheKey.split("-");
        WikipediaLanguage lang = WikipediaLanguage.valueOfCode(tokens[0]);
        String username = tokens[1];
        // In case of empty result return a fake user with no groups
        return userService.findUser(lang, username).orElse(ReplacerUser.builder().lang(lang).name(username).build());
    }
}
