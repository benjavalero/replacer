package es.bvalero.replacer.authentication.userrights;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ForbiddenException;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaPageRepository;
import es.bvalero.replacer.wikipedia.WikipediaUser;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.TestOnly;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CheckUserRightsService {

    @Autowired
    private WikipediaPageRepository wikipediaPageRepository;

    @Setter(onMethod_ = @TestOnly)
    @Value("${replacer.admin.user}")
    private String adminUser;

    // Cache the users which try to access features needing special rights
    // This map can grow. We use Caffeine cache to clean periodically the old or obsolete users.
    private final Cache<String, WikipediaUser> cachedUsers = Caffeine
        .newBuilder()
        .expireAfterWrite(1, TimeUnit.DAYS)
        .build();

    public void validateAdminUser(String user) throws ForbiddenException {
        if (!isAdmin(user)) {
            LOGGER.error("Unauthorized user: {}", user);
            throw new ForbiddenException();
        }
    }

    public boolean isAdmin(String username) {
        return this.adminUser.equals(username);
    }

    public void validateBotUser(WikipediaLanguage lang, String user) throws ForbiddenException {
        if (!isBot(lang, user)) {
            LOGGER.error("Unauthorized bot user: {} - {}", lang, user);
            throw new ForbiddenException();
        }
    }

    @VisibleForTesting
    boolean isBot(WikipediaLanguage lang, String username) {
        return Objects.requireNonNull(cachedUsers.get(buildCacheKey(lang, username), this::getWikipediaUser)).isBot();
    }

    private String buildCacheKey(WikipediaLanguage lang, String username) {
        return String.format("%s-%s", lang.getCode(), username);
    }

    private WikipediaUser getWikipediaUser(String cacheKey) {
        String[] tokens = cacheKey.split("-");
        WikipediaLanguage lang = WikipediaLanguage.valueOfCode(tokens[0]);
        String username = tokens[1];
        try {
            return wikipediaPageRepository.getWikipediaUser(lang, username);
        } catch (WikipediaException e) {
            // Return a fake user with no groups
            return WikipediaUser.builder().lang(lang).name(username).build();
        }
    }
}
