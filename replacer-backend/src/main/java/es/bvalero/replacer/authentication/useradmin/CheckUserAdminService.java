package es.bvalero.replacer.authentication.useradmin;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaService;
import es.bvalero.replacer.wikipedia.WikipediaUser;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.Setter;
import org.jetbrains.annotations.TestOnly;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CheckUserAdminService {


    @Autowired
    private WikipediaService wikipediaService;

    @Setter(onMethod_ = @TestOnly)
    @Value("${replacer.admin.user}")
    private String adminUser;

    // Cache the users which try to access features needing special rights
    // This map can grow. We use Caffeine cache to clean periodically the old or obsolete users.
    private final Cache<String, WikipediaUser> cachedUsers = Caffeine
        .newBuilder()
        .expireAfterWrite(1, TimeUnit.DAYS)
        .build();

    public boolean isAdminUser(String username) {
        return this.adminUser.equals(username);
    }

    public boolean isBot(WikipediaLanguage lang, String username) {
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
            return wikipediaService.getWikipediaUser(lang, username);
        } catch (WikipediaException e) {
            // Return a fake user with no groups
            return WikipediaUser.builder().lang(lang).name(username).build();
        }
    }
}
